
package com.sujeongring.domain.matching.service.NormalMatching;

import com.sujeongring.domain.currency.exception.CurrencyErrorCode;
import com.sujeongring.domain.currency.exception.CurrencyException;
import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.entity.MatchUsage;
import com.sujeongring.domain.matching.enums.MatchUsageType;
import com.sujeongring.domain.matching.exception.DailyFreeMatchingLimitExceededException;
import com.sujeongring.domain.matching.repository.MatchUsageRepository;
import com.sujeongring.domain.matching.dto.response.DailyFreeMatchingStatusResponse;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.domain.currency.entity.Wallet;
import com.sujeongring.domain.currency.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

// 무료 매칭 횟수(주 3회) 관리 및 검사
@Service
@RequiredArgsConstructor
public class DailyFreeMatchingService {

    public static final int WEEKLY_FREE_LIMIT = 3;
    private static final int PAID_MATCH_COST = 30;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MatchUsageRepository matchUsageRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    // 매칭 이용 상태 조회
    @Transactional(readOnly = true)
    public DailyFreeMatchingStatusResponse getStatus(Long userId) {

        LocalDate today = LocalDate.now(KST);

        // 이번 주 무료 매칭 사용 횟수
        long used = countFreeUses(userId, today);

        // 남은 무료 매칭 횟수
        int remaining = (int) Math.max(
                0,
                WEEKLY_FREE_LIMIT - used
        );

        // 무료 매칭 상태
        DailyFreeMatchingStatusResponse.WeeklyMatching weeklyMatching =
                new DailyFreeMatchingStatusResponse.WeeklyMatching(
                        remaining,
                        WEEKLY_FREE_LIMIT
                );

        // 수정구 지갑 조회
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CurrencyException(CurrencyErrorCode.WALLET_NOT_FOUND)
                );

        int balance = wallet.getBalance();

        // 수정구 잔액 및 매칭 비용
        DailyFreeMatchingStatusResponse.Currency currency =
                new DailyFreeMatchingStatusResponse.Currency(
                        balance,
                        PAID_MATCH_COST
                );

        // 무료 횟수가 남아 있으면 FREE, 없으면 CURRENCY
        String actionType = remaining > 0 ? "FREE" : "CURRENCY";

        return new DailyFreeMatchingStatusResponse(
                weeklyMatching,
                currency,
                actionType
        );
    }

    // 무료 매칭 성공 시 사용 기록 저장
    @Transactional
    public MatchUsage recordSuccessfulFreeMatch(
            Long userId,
            MatchRecommendation recommendation
    ) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new IllegalStateException(
                    "쓰기 트랜잭션에서 호출해야 합니다."
            );
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다: " + userId
                        )
                );

        if (!recommendation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "추천 기록의 소유자가 일치하지 않습니다."
            );
        }

        long used = countFreeUses(
                userId,
                LocalDate.now(KST)
        );

        if (used >= WEEKLY_FREE_LIMIT) {
            throw new DailyFreeMatchingLimitExceededException();
        }
        return matchUsageRepository.save(
                new MatchUsage(
                        user,
                        recommendation,
                        MatchUsageType.FREE
                )
        );
    }

    private long countFreeUses(Long userId, LocalDate date) {

        LocalDate monday = date.with(
                TemporalAdjusters.previousOrSame(
                        DayOfWeek.MONDAY
                )
        );

        LocalDateTime start = monday.atStartOfDay();
        LocalDateTime end = monday.plusWeeks(1).atStartOfDay();

        return matchUsageRepository
                .countByUser_IdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        userId,
                        MatchUsageType.FREE,
                        start,
                        end
                );
    }

}
