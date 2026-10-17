
package com.sujeongring.domain.matching.service;

import com.sujeongring.domain.currency.service.CurrencyService;
import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.entity.MatchUsage;
import com.sujeongring.domain.matching.enums.MatchUsageType;
import com.sujeongring.domain.matching.enums.MatchingType;
import com.sujeongring.domain.matching.exception.MatchingErrorCode;
import com.sujeongring.domain.matching.repository.MatchRecommendationRepository;
import com.sujeongring.domain.matching.repository.MatchUsageRepository;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.enums.UserStatus;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.global.error.exception.BaseException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final CurrencyService currencyService;
    private static final int DAILY_FREE_LIMIT = 3;
    private static final ZoneId KOREA_ZONE =
            ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final MatchRecommendationRepository matchRecommendationRepository;
    private final MatchUsageRepository matchUsageRepository;
    private final MatchingScoreService matchingScoreService;

    @Transactional
    public MatchRecommendation matchNormal(Long userId) {

        // 1. 사용자 조회
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BaseException(
                        MatchingErrorCode.USER_NOT_FOUND
                ));

        // 2. 온보딩 완료 여부 확인
        if (!user.isOnboardingCompleted()) {
            throw new BaseException(
                    MatchingErrorCode.ONBOARDING_NOT_COMPLETED
            );
        }

        // 3. 활성 사용자 여부 확인
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BaseException(
                    MatchingErrorCode.USER_NOT_ACTIVE
            );
        }

        // 4. 오늘 무료 매칭 사용 횟수 조회
        LocalDate today = LocalDate.now(KOREA_ZONE);

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long usedCount =
                matchUsageRepository
                        .countByUserIdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                userId,
                                MatchUsageType.FREE,
                                start,
                                end
                        );

        // 5. 하루 무료 매칭 3회 제한
        MatchUsageType usageType;

        if (usedCount < DAILY_FREE_LIMIT) {
            usageType = MatchUsageType.FREE;
        } else {
            usageType = MatchUsageType.PAID;
        }

        // 6. 추천 가능한 후보 조회
        List<User> candidates =
                userRepository.findEligibleMatchingCandidates(
                        userId,
                        UserStatus.ACTIVE
                );

        if (candidates.isEmpty()) {
            throw new BaseException(
                    MatchingErrorCode.NO_MATCHING_CANDIDATE
            );
        }

        // 7. 유사도가 가장 높은 후보 선정
        User candidate = candidates.stream()
                .max(Comparator.comparingDouble(
                        c -> matchingScoreService.calculateScore(user, c)
                ))
                .orElseThrow(() -> new BaseException(
                        MatchingErrorCode.NO_MATCHING_CANDIDATE
                ));

        // 8. 유사도 점수 계산
        int score = (int) Math.round(
                matchingScoreService.calculateScore(user, candidate)
        );

        // 9. 추천 결과 저장
        MatchRecommendation recommendation =
                matchRecommendationRepository.save(
                        new MatchRecommendation(
                                user,
                                candidate,
                                MatchingType.NORMAL,
                                score,
                                null
                        )
                );

        // 10. 무료 매칭 사용 기록 저장
        matchUsageRepository.save(
                new MatchUsage(
                        user,
                        recommendation,
                        usageType
                )
        );

        return recommendation;
    }
}
