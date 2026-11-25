package com.sujeongring.domain.user.service;

import com.sujeongring.domain.currency.entity.Wallet;
import com.sujeongring.domain.currency.repository.WalletRepository;
import com.sujeongring.domain.user.dto.response.MatchingProfileResponse;
import com.sujeongring.domain.user.dto.response.MyPageHomeResponse;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.exception.UserErrorCode;
import com.sujeongring.domain.user.repository.SajuProfileRepository;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.domain.user.dto.request.UpdateMyProfileRequest;
import com.sujeongring.domain.user.dto.response.UpdateMyProfileResponse;
import com.sujeongring.domain.user.repository.onboarding.UserFriendTypeRepository;
import com.sujeongring.domain.user.repository.onboarding.UserHobbyRepository;
import com.sujeongring.domain.user.repository.onboarding.UserInterestRepository;
import com.sujeongring.domain.user.repository.onboarding.UserSocialStyleRepository;
import com.sujeongring.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sujeongring.domain.user.entity.UserSocialStyle;
import com.sujeongring.domain.user.entity.UserInterest;
import com.sujeongring.domain.user.entity.UserHobby;
import com.sujeongring.domain.user.entity.UserFriendType;
import com.sujeongring.domain.user.dto.request.MatchingProfileUpdateRequest;

import java.util.HashSet;
import java.util.Objects;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final UserSocialStyleRepository socialStyleRepository;
    private final UserInterestRepository interestRepository;
    private final UserHobbyRepository hobbyRepository;
    private final UserFriendTypeRepository friendTypeRepository;
    private final SajuProfileRepository sajuProfileRepository;

    public MyPageHomeResponse getMyPageHome(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String studentNumber = user.getStudentNumber();

        if (studentNumber == null ||
                !studentNumber.matches("^\\d{4}\\d+$")) {
            throw new IllegalStateException("학번 형식이 올바르지 않습니다.");
        }

        int admissionYear =
                Integer.parseInt(studentNumber.substring(0, 4));

        return new MyPageHomeResponse(
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getDepartmentName(),
                studentNumber,
                admissionYear
        );
    }


    @Transactional
    public UpdateMyProfileResponse updateMyProfile(
            Long userId,
            UpdateMyProfileRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 닉네임 유효성 검사
        if (request.nickname() != null) {
            if (request.nickname().isBlank()
                    || request.nickname().length() > 30) {
                throw new IllegalArgumentException(
                        "닉네임은 1~30자 이내여야 합니다.");
            }
        }

        // 학과명 유효성 검사
        if (request.departmentName() != null) {
            if (request.departmentName().isBlank()
                    || request.departmentName().length() > 50) {
                throw new IllegalArgumentException(
                        "학과명은 1~50자 이내여야 합니다.");
            }
        }

        // 전달된 값만 변경
        user.updateProfile(
                request.nickname(),
                request.departmentName()
        );

        return new UpdateMyProfileResponse(
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getDepartmentName()
        );
    }

    // 매칭 정보 조회
    public MatchingProfileResponse getMatchingProfile(Long userId) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BaseException(
                                UserErrorCode.USER_NOT_FOUND
                        )
                );

        // 2. 사람을 만나는 방식 조회
        List<MatchingProfileResponse.Option> socialStyles =
                socialStyleRepository
                        .findAllByUser_IdIn(List.of(userId))
                        .stream()
                        .map(UserSocialStyle::getSocialStyle)
                        .map(style ->
                                new MatchingProfileResponse.Option(
                                        style.name(),
                                        style.getLabel()
                                )
                        )
                        .toList();

        // 3. 관심 카테고리 조회
        List<MatchingProfileResponse.Option> interests =
                interestRepository
                        .findAllByUser_IdIn(List.of(userId))
                        .stream()
                        .map(UserInterest::getInterest)
                        .map(interest ->
                                new MatchingProfileResponse.Option(
                                        interest.name(),
                                        interest.getLabel()
                                )
                        )
                        .toList();

        // 4. 즐겨하는 취미 조회
        List<MatchingProfileResponse.Option> hobbies =
                hobbyRepository
                        .findAllByUser_IdIn(List.of(userId))
                        .stream()
                        .map(UserHobby::getHobby)
                        .map(hobby ->
                                new MatchingProfileResponse.Option(
                                        hobby.name(),
                                        hobby.getLabel()
                                )
                        )
                        .toList();

        // 5. 만나고 싶은 친구 유형 조회
        List<MatchingProfileResponse.Option> friendTypes =
                friendTypeRepository
                        .findAllByUser_IdIn(List.of(userId))
                        .stream()
                        .map(UserFriendType::getFriendType)
                        .map(type ->
                                new MatchingProfileResponse.Option(
                                        type.name(),
                                        type.getLabel()
                                )
                        )
                        .toList();

        // 6. 사주 매칭 정보 조회
        MatchingProfileResponse.Saju saju =
                sajuProfileRepository.findByUserId(userId)
                        .map(profile ->
                                new MatchingProfileResponse.Saju(
                                        profile.isMatchingEnabled(),
                                        profile.getBirthDate(),
                                        profile.getCalendarType(),
                                        profile.getBirthTime()
                                )
                        )
                        .orElseGet(() ->
                                new MatchingProfileResponse.Saju(
                                        false,
                                        null,
                                        null,
                                        null
                                )
                        );

        // 7. 최종 응답 반환
        return new MatchingProfileResponse(
                user.getMbti(),
                socialStyles,
                interests,
                hobbies,
                friendTypes,
                saju
        );
    }

    // 매칭 정보 수정
    @Transactional
    public MatchingProfileResponse updateMatchingProfile(
            Long userId,
            MatchingProfileUpdateRequest request
    ) {

        // 1. 사용자 조회 및 동시 수정 방지
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new BaseException(
                                UserErrorCode.USER_NOT_FOUND
                        )
                );

        // 2. 요청 데이터 검증
        validateMatchingProfileRequest(request);

        // 3. MBTI 수정
        if (request.mbti() != null) {

            user.updateMbti(request.mbti());
        }

        // 4. 사람을 만나는 방식 수정
        if (request.socialStyles() != null) {

            socialStyleRepository.deleteAll(
                    socialStyleRepository
                            .findAllByUser_IdIn(List.of(userId))
            );

            socialStyleRepository.flush();

            socialStyleRepository.saveAll(
                    request.socialStyles()
                            .stream()
                            .map(style ->
                                    new UserSocialStyle(user, style)
                            )
                            .toList()
            );
        }

        // 5. 관심 카테고리 수정
        if (request.interests() != null) {

            interestRepository.deleteAll(
                    interestRepository
                            .findAllByUser_IdIn(List.of(userId))
            );

            interestRepository.flush();

            interestRepository.saveAll(
                    request.interests()
                            .stream()
                            .map(interest ->
                                    new UserInterest(user, interest)
                            )
                            .toList()
            );
        }

        // 6. 즐겨하는 취미 수정
        if (request.hobbies() != null) {

            hobbyRepository.deleteAll(
                    hobbyRepository
                            .findAllByUser_IdIn(List.of(userId))
            );

            hobbyRepository.flush();

            hobbyRepository.saveAll(
                    request.hobbies()
                            .stream()
                            .map(hobby ->
                                    new UserHobby(user, hobby)
                            )
                            .toList()
            );
        }

        // 7. 만나고 싶은 친구 유형 수정
        if (request.friendTypes() != null) {

            friendTypeRepository.deleteAll(
                    friendTypeRepository
                            .findAllByUser_IdIn(List.of(userId))
            );

            friendTypeRepository.flush();

            friendTypeRepository.saveAll(
                    request.friendTypes()
                            .stream()
                            .map(type ->
                                    new UserFriendType(user, type)
                            )
                            .toList()
            );
        }

        // 8. 수정된 매칭 정보 반환
        return getMatchingProfile(userId);
    }

    // 매칭 정보 수정 요청 검증
    private void validateMatchingProfileRequest(
            MatchingProfileUpdateRequest request
    ) {

        // 수정할 항목이 없는 경우
        if (request == null ||
                (request.mbti() == null &&
                        request.socialStyles() == null &&
                        request.interests() == null &&
                        request.hobbies() == null &&
                        request.friendTypes() == null)) {

            throw new BaseException(
                    UserErrorCode.ONBOARDING_INVALID_SELECTION,
                    "수정할 매칭 정보를 입력해주세요."
            );
        }

        if (request.socialStyles() != null) {
            validateList(request.socialStyles(), 1, 2, "만남 방식");
        }

        if (request.interests() != null) {
            validateList(request.interests(), 3, 5, "관심사");
        }

        if (request.hobbies() != null) {
            validateList(request.hobbies(), 3, 5, "취미");
        }

        if (request.friendTypes() != null) {
            validateList(request.friendTypes(), 1, 3, "친구 유형");
        }
    }


    // 선택 개수 및 중복 검증
    private <T> void validateList(
            List<T> values,
            int min,
            int max,
            String fieldName
    ) {

        // 선택 개수 검증
        if (values.size() < min ||
                values.size() > max) {

            throw new BaseException(
                    UserErrorCode.ONBOARDING_INVALID_SELECTION,
                    fieldName + "은(는) 최소 " + min +
                            "개, 최대 " + max +
                            "개까지 선택할 수 있습니다."
            );
        }

        // null 및 중복값 검증
        if (values.stream().anyMatch(Objects::isNull) ||
                new HashSet<>(values).size() != values.size()) {

            throw new BaseException(
                    UserErrorCode.ONBOARDING_INVALID_SELECTION,
                    fieldName + "에 중복되거나 유효하지 않은 값이 있습니다."
            );
        }
    }
}





