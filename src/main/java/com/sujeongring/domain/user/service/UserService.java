package com.sujeongring.domain.user.service;

//마이페이지
import com.sujeongring.domain.currency.entity.Wallet;
import com.sujeongring.domain.currency.repository.WalletRepository;
import com.sujeongring.domain.user.dto.response.MyPageHomeResponse;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.repository.UserRepository;
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

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalStateException("수정구 지갑을 찾을 수 없습니다."));

        // 학번 표시 형식 변환
        String studentNumber = user.getStudentNumber();

        String admissionYear = studentNumber.substring(2, 4) + "학번";

        int blockedUserCount = 0; //TODO:차단 기능 구현 후 실제 차단 수 조회. 현재 임시값 0 사용

        return new MyPageHomeResponse(
                user.getNickname(),
                user.getDepartmentName(),
                admissionYear,
                user.getProfileImageUrl(),
                wallet.getBalance(),
                blockedUserCount
        );
    }
}