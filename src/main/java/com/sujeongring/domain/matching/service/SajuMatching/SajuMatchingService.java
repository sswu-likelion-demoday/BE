package com.sujeongring.domain.matching.service.SajuMatching;

import com.sujeongring.domain.currency.entity.CurrencyTransaction;
import com.sujeongring.domain.currency.entity.Wallet;
import com.sujeongring.domain.currency.enums.CurrencyReason;
import com.sujeongring.domain.currency.enums.CurrencyTransactionType;
import com.sujeongring.domain.currency.repository.CurrencyTransactionRepository;
import com.sujeongring.domain.currency.repository.WalletRepository;

import com.sujeongring.domain.matching.dto.response.MatchingHomeResponse;
import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.entity.MatchUsage;
import com.sujeongring.domain.matching.enums.MatchUsageType;
import com.sujeongring.domain.matching.enums.MatchingType;
import com.sujeongring.domain.matching.repository.MatchRecommendationRepository;
import com.sujeongring.domain.matching.repository.MatchUsageRepository;
import com.sujeongring.domain.matching.service.NormalMatching.DailyFreeMatchingService;
import com.sujeongring.domain.matching.service.NormalMatching.MatchingHomeService;

import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.relationship.repository.RelationshipRepository;
import com.sujeongring.domain.user.entity.SajuProfile;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.enums.UserStatus;
import com.sujeongring.domain.user.repository.BlockRepository;
import com.sujeongring.domain.user.repository.SajuProfileRepository;
import com.sujeongring.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SajuMatchingService {

    private static final int PAID_MATCH_COST = 30;

    private final UserRepository userRepository;
    private final SajuProfileRepository sajuProfileRepository;
    private final MatchRecommendationRepository recommendationRepository;
    private final MatchUsageRepository matchUsageRepository;

    private final DailyFreeMatchingService dailyFreeMatchingService;
    private final MatchingHomeService matchingHomeService;

    private final WalletRepository walletRepository;
    private final CurrencyTransactionRepository currencyTransactionRepository;

    private final BlockRepository blockRepository;
    private final RelationshipRepository relationshipRepository;

    private final SajuCalendarService sajuCalendarService;
    private final SajuCompatibilityCalculator compatibilityCalculator;

    @Transactional
    public MatchRecommendation matchSaju(Long userId) {
        //사용자 검증
        User requester = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        if (requester.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException(
                    "활성 사용자만 매칭할 수 있습니다."
            );
        }

        if (!requester.isOnboardingCompleted()) {
            throw new IllegalStateException(
                    "온보딩을 완료해야 매칭할 수 있습니다."
            );
        }

        // 매칭 슬롯 확인
        MatchingHomeResponse home =
                matchingHomeService.getHome(userId);

        if (home.relationshipSlots().available() <= 0) {
            throw new IllegalStateException(
                    "매칭 슬롯이 모두 사용 중입니다."
            );
        }

        // 내 사주 프로필 조회
        SajuProfile myProfile = sajuProfileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "사주 정보가 등록되지 않았습니다."
                        )
                );

        // 무료 매칭 횟수 확인
        boolean hasFreeMatch = dailyFreeMatchingService
                .getStatus(userId)
                .weeklyMatching()
                .remaining() > 0;

        Wallet wallet = null;

        if (!hasFreeMatch) {
            wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "수정구 지갑이 없습니다."
                            )
                    );

            if (wallet.getBalance() < PAID_MATCH_COST) {
                throw new IllegalStateException(
                        "수정구 잔액이 부족합니다."
                );
            }
        }

        // 이전에 추천받은 후보 제외
        Set<Long> previousCandidateIds =
                recommendationRepository
                        .findAllByUserIdOrderByCreatedAtDesc(userId)
                        .stream()
                        .map(r -> r.getCandidate().getId())
                        .collect(Collectors.toSet());

        // 양방향 차단 사용자 제외
        Set<Long> blockedUserIds = blockRepository
                .findAllByBlockerIdOrBlockedId(userId, userId)
                .stream()
                .map(block -> {
                    Long blockerId = block.getBlocker().getId();

                    return blockerId.equals(userId)
                            ? block.getBlocked().getId()
                            : blockerId;
                })
                .collect(Collectors.toSet());

        // 활성 관계 사용자 제외
        Set<Long> activeRelationshipUserIds =
                relationshipRepository
                        .findAllByStatusAndUserAIdOrStatusAndUserBId(
                                RelationshipStatus.ACTIVE,
                                userId,
                                RelationshipStatus.ACTIVE,
                                userId
                        )
                        .stream()
                        .map(relationship ->
                                relationship.getUserA().getId()
                                        .equals(userId)
                                        ? relationship.getUserB().getId()
                                        : relationship.getUserA().getId()
                        )
                        .collect(Collectors.toSet());

        // 매칭 가능한 사용자 필터링
        List<User> candidates = userRepository.findAll()
                .stream()
                .filter(user -> !user.getId().equals(userId))
                .filter(User::isOnboardingCompleted)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .filter(user -> !previousCandidateIds.contains(user.getId()))
                .filter(user -> !blockedUserIds.contains(user.getId()))
                .filter(user -> !activeRelationshipUserIds.contains(user.getId()))

                .filter(user ->
                        sajuProfileRepository.findByUserId(user.getId())
                                .map(profile ->
                                        profile.isMatchingEnabled()
                                                && profile.getCalendarType() != null
                                                && profile.getBirthDate() != null
                                )
                                .orElse(false)
                )
                .toList();

        if (candidates.isEmpty()) {
            throw new IllegalStateException(
                    "현재 사주 매칭 가능한 사용자가 없습니다."
            );
        }

        // 내 간지 계산
        String myGapja = sajuCalendarService.calculateGapja(myProfile);

        // 후보별 궁합 점수 계산
        Map<Long, Integer> scores = candidates.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        candidate -> {
                            SajuProfile candidateProfile =
                                    sajuProfileRepository
                                            .findByUserId(candidate.getId())
                                            .orElseThrow();

                            String candidateGapja =
                                    sajuCalendarService
                                            .calculateGapja(candidateProfile);

                            return compatibilityCalculator.calculate(
                                    myGapja,
                                    candidateGapja
                            );
                        }
                ));

        // 궁합 점수가 가장 높은 후보 선정
        User selected = candidates.stream()
                .max(
                        Comparator
                                .comparingInt(
                                        (User user) ->
                                                scores.get(user.getId())
                                )
                                .thenComparing(
                                        User::getId,
                                        Comparator.reverseOrder()
                                )
                )
                .orElseThrow();

        int compatibilityScore = scores.get(selected.getId());

        MatchRecommendation recommendation =
                recommendationRepository.save(
                        new MatchRecommendation(
                                requester,
                                selected,
                                MatchingType.SAJU,
                                null,
                                compatibilityScore,
                                "사주 궁합을 바탕으로 추천드려요"
                        )
                );

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

        return recommendation;
    }
}
