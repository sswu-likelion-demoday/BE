package com.sujeongring.domain.matching.service.NormalMatching;

import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.entity.MatchUsage;
import com.sujeongring.domain.matching.enums.MatchUsageType;
import com.sujeongring.domain.matching.exception.DailyFreeMatchingLimitExceededException;
import com.sujeongring.domain.matching.repository.MatchUsageRepository;
import com.sujeongring.domain.matching.dto.response.DailyFreeMatchingStatusResponse;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

//무료 매칭 횟수(기본 3회) 관리하고 검사
@Service
@RequiredArgsConstructor
public class DailyFreeMatchingService {
    public static final int DAILY_FREE_LIMIT = 3;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MatchUsageRepository matchUsageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DailyFreeMatchingStatusResponse getStatus(Long userId) {
        LocalDate today = LocalDate.now(KST);
        long used = countFreeUses(userId, today);
        int remaining = (int) Math.max(0, DAILY_FREE_LIMIT - used);
        return new DailyFreeMatchingStatusResponse(today, DAILY_FREE_LIMIT, used, remaining);
    }

    @Transactional
    public MatchUsage recordSuccessfulFreeMatch(Long userId, MatchRecommendation recommendation) {
        if (!org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()
                || org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new IllegalStateException("쓰기 트랜잭션에서 호출해야 합니다.");
        }
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        if (!recommendation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("추천 기록의 소유자가 일치하지 않습니다.");
        }
        if (countFreeUses(userId, LocalDate.now(KST)) >= DAILY_FREE_LIMIT) {
            throw new DailyFreeMatchingLimitExceededException();
        }
        return matchUsageRepository.save(new MatchUsage(user, recommendation, MatchUsageType.FREE));
    }

    private long countFreeUses(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return matchUsageRepository.countByUser_IdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                userId, MatchUsageType.FREE, start, end);
    }
}
