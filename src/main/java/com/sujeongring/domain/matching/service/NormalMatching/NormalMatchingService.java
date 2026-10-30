package com.sujeongring.domain.matching.service.NormalMatching;

import com.sujeongring.domain.currency.entity.CurrencyTransaction;
import com.sujeongring.domain.currency.entity.Wallet;
import com.sujeongring.domain.currency.enums.CurrencyReason;
import com.sujeongring.domain.currency.enums.CurrencyTransactionType;
import com.sujeongring.domain.currency.exception.CurrencyErrorCode;
import com.sujeongring.domain.currency.exception.CurrencyException;
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
import com.sujeongring.domain.user.repository.BlockRepository;
import com.sujeongring.domain.user.entity.Block;
import com.sujeongring.domain.relationship.repository.RelationshipRepository;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.relationship.service.RelationshipSlotService;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
    private final BlockRepository blockRepository;
    private final RelationshipRepository relationshipRepository;
    private final MatchingHomeService matchingHomeService;
    private final RelationshipSlotService relationshipSlotService;

    @Transactional
    public MatchRecommendation matchNormal(Long userId) {

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

        relationshipSlotService.validateAvailableSlot(userId);

        boolean hasFreeMatch =
                dailyFreeMatchingService.getStatus(userId)
                        .weeklyMatching().remaining() > 0;

        Wallet wallet = null;

        if (!hasFreeMatch) {
            wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() ->
                            new CurrencyException(CurrencyErrorCode.WALLET_NOT_FOUND)
                    );

            if (wallet.getBalance() < PAID_MATCH_COST) {
                throw new CurrencyException(CurrencyErrorCode.INSUFFICIENT_CURRENCY_BALANCE);
            }
        }

        // 이전에 추천받은 후보 제외
        Set<Long> previousCandidateIds = recommendationRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(r -> r.getCandidate().getId())
                .collect(Collectors.toSet());

        // 내가 차단했거나 나를 차단한 사용자 ID 수집
        Set<Long> blockedUserIds = blockRepository
                .findAllByBlockerIdOrBlockedId(userId, userId)
                .stream()
                .map(block -> {
                    Long blockerId = block.getBlocker().getId();

                    if (blockerId.equals(userId)) {
                        // 내가 상대방을 차단한 경우
                        return block.getBlocked().getId();
                    } else {
                        // 상대방이 나를 차단한 경우
                        return blockerId;
                    }
                })
                .collect(Collectors.toSet());


        // 현재 나와 ACTIVE 관계인 사용자 ID 수집
        Set<Long> activeRelationshipUserIds = relationshipRepository
                .findAllByStatusAndUserAIdOrStatusAndUserBId(
                        RelationshipStatus.ACTIVE,
                        userId,
                        RelationshipStatus.ACTIVE,
                        userId
                )
                .stream()
                .map(relationship -> {
                    Long userAId = relationship.getUserA().getId();

                    if (userAId.equals(userId)) {
                        // 내가 userA라면 상대방은 userB
                        return relationship.getUserB().getId();
                    } else {
                        // 내가 userB라면 상대방은 userA
                        return userAId;
                    }
                })
                .collect(Collectors.toSet());


        // 매칭 가능 사용자 조회
        List<User> candidates = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(userId))
                .filter(User::isOnboardingCompleted)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .filter(user -> !previousCandidateIds.contains(user.getId()))
                .filter(user -> !blockedUserIds.contains(user.getId()))
                .filter(user -> !activeRelationshipUserIds.contains(user.getId()))
                .toList();

        if (candidates.isEmpty()) {
            throw new IllegalStateException("현재 매칭 가능한 사용자가 없습니다.");
        }

        // 온보딩 유사도 계산
        List<Long> candidateIds = candidates.stream()
                .map(User::getId)
                .toList();

        Map<Long, Integer> scores =
                matchingService.scoreCandidates(userId, candidateIds);

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

        String introduction = matchingService.generateMatchingReason(
                userId,
                selected.getId()
        );

        MatchRecommendation recommendation =
                recommendationRepository.save(
                        new MatchRecommendation(
                                requester,
                                selected,
                                MatchingType.NORMAL,
                                scores.getOrDefault(selected.getId(), 0),
                                null,
                                introduction
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