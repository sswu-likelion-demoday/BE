package com.sujeongring.domain.auth.service;

import com.sujeongring.domain.auth.dto.request.SignupRequest;
import com.sujeongring.domain.auth.dto.response.SignupResponse;
import com.sujeongring.domain.auth.dto.response.StudentNumberCheckResponse;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.global.error.ErrorCode;
import com.sujeongring.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String VERIFIED_KEY_PREFIX = "email:verified:";

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    /**
     * 학번 중복 확인
     */
    @Transactional(readOnly = true)
    public StudentNumberCheckResponse checkStudentNumber(
            String studentNumber
    ) {
        boolean exists =
                userRepository.existsByStudentNumber(studentNumber);

        return new StudentNumberCheckResponse(!exists);
    }

    /**
     * 회원가입
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {


        // 이메일 인증 여부 확인

        String verifiedKey =
                VERIFIED_KEY_PREFIX + request.email();

        String verified =
                redisTemplate.opsForValue().get(verifiedKey);

        if (!"true".equals(verified)) {
            throw new BaseException(
                    ErrorCode.AUTH_EMAIL_NOT_VERIFIED
            );
        }

        // 이메일에 포함된 학번과 요청 학번 일치 확인
        String emailStudentNumber =
                request.email().substring(
                        0,
                        request.email().indexOf("@")
                );

        if (!emailStudentNumber.equals(request.studentNumber())) {
            throw new BaseException(
                    ErrorCode.AUTH_STUDENT_NUMBER_MISMATCH
            );
        }

        // 학번 중복 재확인
        if (userRepository.existsByStudentNumber(request.studentNumber())) {
            throw new BaseException(
                    ErrorCode.AUTH_DUPLICATE_STUDENT_NUMBER
            );
        }

        // 3. 비밀번호 암호화
        String encodedPassword =
                passwordEncoder.encode(request.password());

        // 4. User 생성
        User user = new User(
                request.name(),
                request.nickname(),
                encodedPassword,
                request.studentNumber(),
                request.departmentName()
        );

        // DB 저장
        User savedUser = userRepository.save(user);

        // 이메일 인증 정보 삭제
        redisTemplate.delete(verifiedKey);

        // 응답
        return new SignupResponse(
                savedUser.getId(),
                savedUser.getNickname(),
                savedUser.isOnboardingCompleted()
        );
    }
}
