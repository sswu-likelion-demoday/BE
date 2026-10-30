package com.sujeongring.domain.user.service;

//마이페이지
import com.sujeongring.domain.currency.entity.Wallet;
import com.sujeongring.domain.currency.repository.WalletRepository;
import com.sujeongring.domain.user.dto.response.MyPageHomeResponse;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.domain.user.dto.request.UpdateMyProfileRequest;
import com.sujeongring.domain.user.dto.response.UpdateMyProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

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

}




