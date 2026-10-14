
package com.sujeongring.domain.matching.service;

import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.enums.MatchingType;
import com.sujeongring.domain.matching.exception.MatchingErrorCode;
import com.sujeongring.domain.matching.repository.MatchRecommendationRepository;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.enums.UserStatus;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.global.error.exception.BaseException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final UserRepository userRepository;
    private final MatchRecommendationRepository matchRecommendationRepository;

    @Transactional
    public MatchRecommendation matchNormal(Long userId) {

        // 1. 현재 사용자 조회
        User user = userRepository.findById(userId)
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

        // 4. 매칭 가능한 후보 조회
        List<User> candidates =
                userRepository.findEligibleMatchingCandidates(
                        userId,
                        UserStatus.ACTIVE
                );

        // 5. 매칭 후보가 없는 경우
        if (candidates.isEmpty()) {
            throw new BaseException(
                    MatchingErrorCode.NO_MATCHING_CANDIDATE
            );
        }

        // 6. 랜덤으로 후보 선정
        int randomIndex = ThreadLocalRandom.current()
                .nextInt(candidates.size());

        User candidate = candidates.get(randomIndex);

        // 7. 매칭 추천 객체 생성
        MatchRecommendation recommendation =
                new MatchRecommendation(
                        user,
                        candidate,
                        MatchingType.NORMAL,
                        null,
                        null
                );

        // 8. 매칭 추천 결과 저장
        return matchRecommendationRepository.save(recommendation);
    }
}
