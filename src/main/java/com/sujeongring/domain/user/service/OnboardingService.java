
package com.sujeongring.domain.user.service;

import com.sujeongring.domain.user.dto.request.OnboardingCompleteRequest;
import com.sujeongring.domain.user.dto.response.OnboardingCompleteResponse;

import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.entity.UserFriendType;
import com.sujeongring.domain.user.entity.UserHobby;
import com.sujeongring.domain.user.entity.UserInterest;
import com.sujeongring.domain.user.entity.UserSocialStyle;

import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.domain.user.repository.onboarding.UserFriendTypeRepository;
import com.sujeongring.domain.user.repository.onboarding.UserHobbyRepository;
import com.sujeongring.domain.user.repository.onboarding.UserInterestRepository;
import com.sujeongring.domain.user.repository.onboarding.UserSocialStyleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

import com.sujeongring.domain.user.exception.UserErrorCode;
import com.sujeongring.global.error.exception.BaseException;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;
    private final UserSocialStyleRepository socialStyleRepository;
    private final UserInterestRepository interestRepository;
    private final UserHobbyRepository hobbyRepository;
    private final UserFriendTypeRepository friendTypeRepository;

    @Transactional
    public OnboardingCompleteResponse complete(
            Long userId,
            OnboardingCompleteRequest request
    ) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BaseException(UserErrorCode.USER_NOT_FOUND));

        // 2. 온보딩 중복 완료 방지
        if (user.isOnboardingCompleted()) {
            throw new BaseException(
                    UserErrorCode.ONBOARDING_ALREADY_COMPLETED
            );
        }

        // 3. 요청 데이터 검증
        validateRequest(request);

        // 4. 만남 방식 저장
        List<UserSocialStyle> socialStyles = request.socialStyles()
                .stream()
                .map(style -> new UserSocialStyle(user, style))
                .toList();

        socialStyleRepository.saveAll(socialStyles);

        // 5. 관심사 저장
        List<UserInterest> interests = request.interests()
                .stream()
                .map(interest -> new UserInterest(user, interest))
                .toList();

        interestRepository.saveAll(interests);

        // 6. 취미 저장
        List<UserHobby> hobbies = request.hobbies()
                .stream()
                .map(hobby -> new UserHobby(user, hobby))
                .toList();

        hobbyRepository.saveAll(hobbies);

        // 7. 선호 친구 유형 저장
        List<UserFriendType> friendTypes = request.friendTypes()
                .stream()
                .map(type -> new UserFriendType(user, type))
                .toList();

        friendTypeRepository.saveAll(friendTypes);

        // 8. MBTI 및 온보딩 완료 상태 변경
        user.completeOnboarding(request.mbti());

        // 9. 응답 반환
        return new OnboardingCompleteResponse(
                user.getId(),
                user.isOnboardingCompleted()
        );
    }


    private void validateRequest(OnboardingCompleteRequest request) {

        if (request == null || request.mbti() == null) {
            throw new BaseException(
                    UserErrorCode.ONBOARDING_INVALID_SELECTION,
                    "MBTI는 필수입니다."
            );
        }

        validateList(request.socialStyles(), 1, 2, "만남 방식");
        validateList(request.interests(), 3, 5, "관심사");
        validateList(request.hobbies(), 3, 5, "취미");
        validateList(request.friendTypes(), 1, 3, "친구 유형");
    }

    private <T> void validateList(
            List<T> values,
            int min,
            int max,
            String fieldName
    ) {
        if (values == null ||
                values.size() < min ||
                values.size() > max) {

            throw new BaseException(
                    UserErrorCode.ONBOARDING_INVALID_SELECTION,
                    fieldName + " 선택 개수가 올바르지 않습니다."
            );
        }

        if (values.contains(null) ||
                new HashSet<>(values).size() != values.size()) {

            throw new BaseException(
                    UserErrorCode.ONBOARDING_INVALID_SELECTION,
                    fieldName + "에 중복되거나 유효하지 않은 값이 있습니다."
            );
        }
    }
}

