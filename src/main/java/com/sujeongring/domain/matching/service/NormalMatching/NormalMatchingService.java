package com.sujeongring.domain.matching.service.NormalMatching;

import com.sujeongring.domain.currency.entity.CurrencyTransaction;
import com.sujeongring.domain.currency.entity.Wallet;
import com.sujeongring.domain.currency.enums.CurrencyReason;
import com.sujeongring.domain.currency.enums.CurrencyTransactionType;
import com.sujeongring.domain.currency.repository.CurrencyTransactionRepository;
import com.sujeongring.domain.currency.repository.WalletRepository;
import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.entity.MatchUsage;
import com.sujeongring.domain.matching.enums.MatchUsageType;
import com.sujeongring.domain.matching.enums.MatchingType;
import com.sujeongring.domain.matching.repository.MatchRecommendationRepository;
import com.sujeongring.domain.matching.repository.MatchUsageRepository;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.enums.UserStatus;
import com.sujeongring.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NormalMatchingService {

    private static final int PAID_MATCH_COST = 30;

    private final UserRepository userRepository;
    private final MatchRecommendationRepository recommendationRepository;
    private final MatchingService matchingService;
    private final DailyFreeMatchingService dailyFreeMatchingService;

    private final WalletRepository walletRepository;
    private final CurrencyTransactionRepository currencyTransactionRepository;
    private final MatchUsageRepository matchUsageRepository;

    @Transactional
    public MatchRecommendation matchNormal(Long userId) {

        // 1. 사용자 조회 및 잠금
        User requester = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다.")
                );

        if (requester.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("활성 사용자만 매칭할 수 있습니다.");
        }

        if (!requester.isOnboardingCompleted()) {
            throw new IllegalStateException("온보딩을 완료해야 매칭할 수 있습니다.");
        }

        // 2. 무료 매칭 횟수 확인
        boolean hasFreeMatch =
                dailyFreeMatchingService.getStatus(userId)
                        .freeRemainingToday() > 0;

        // 3. 유료 매칭이라면 수정구 잔액 확인
        Wallet wallet = null;

        if (!hasFreeMatch) {
            wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() ->
                            new IllegalStateException("수정구 지갑이 없습니다.")
                    );

            if (wallet.getBalance() < PAID_MATCH_COST) {
                throw new IllegalStateException("수정구 잔액이 부족합니다.");
            }
        }

        // 4. 이전에 추천받은 후보 제외
        Set<Long> previousCandidateIds = recommendationRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(r -> r.getCandidate().getId())
                .collect(Collectors.toSet());

        // 5. 매칭 가능한 사용자 조회
        List<User> candidates = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(userId))
                .filter(User::isOnboardingCompleted)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .filter(user -> !previousCandidateIds.contains(user.getId()))
                .toList();

        if (candidates.isEmpty()) {
            throw new IllegalStateException("현재 매칭 가능한 사용자가 없습니다.");
        }

        // 6. 온보딩 유사도 계산
        List<Long> candidateIds = candidates.stream()
                .map(User::getId)
                .toList();

        Map<Long, Integer> scores =
                matchingService.scoreCandidates(userId, candidateIds);

        // 7. 가장 높은 점수의 사용자 선택
        User selected = candidates.stream()
                .max(Comparator
                        .comparingInt(
                                (User user) -> scores.getOrDefault(user.getId(), 0)
                        )
                        .thenComparing(
                                User::getId,
                                Comparator.reverseOrder()
                        ))
                .orElseThrow();

        // 8. 추천 기록 저장
        MatchRecommendation recommendation =
                recommendationRepository.save(
                        new MatchRecommendation(
                                requester,
                                selected,
                                MatchingType.NORMAL,
                                scores.getOrDefault(selected.getId(), 0),
                                null
                        )
                );

        // 9. 무료 / 유료 매칭 사용 처리
        if (hasFreeMatch) {

            dailyFreeMatchingService.recordSuccessfulFreeMatch(
                    userId,
                    recommendation
            );

        } else {

            wallet.useCurrency(PAID_MATCH_COST);

            currencyTransactionRepository.save(
                    new CurrencyTransaction(
                            wallet,
                            CurrencyTransactionType.USE,
                            CurrencyReason.EXTRA_MATCH,
                            PAID_MATCH_COST,
                            wallet.getBalance()
                    )
            );

            matchUsageRepository.save(
                    new MatchUsage(
                            requester,
                            recommendation,
                            MatchUsageType.PAID
                    )
            );
        }

        // 10. 매칭 결과 반환
        return recommendation;
    }
}