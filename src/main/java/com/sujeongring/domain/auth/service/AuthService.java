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
                userRepository.existsByStudentId(studentNumber);

        return new StudentNumberCheckResponse(!exists);
    }

    /**
     * 회원가입
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        // 1. 이메일 인증 여부 확인
        String verifiedKey =
                VERIFIED_KEY_PREFIX + request.email();

        String verified =
                redisTemplate.opsForValue().get(verifiedKey);

        if (!"true".equals(verified)) {
            throw new BaseException(
                    ErrorCode.AUTH_EMAIL_NOT_VERIFIED
            );
        }

        // 2. 학번 중복 재확인
        if (userRepository.existsByStudentId(
                request.studentNumber()
        )) {
            throw new BaseException(
                    ErrorCode.AUTH_DUPLICATE_STUDENT_NUMBER
            );
        }

        // 3. 비밀번호 암호화
        String encodedPassword =
                passwordEncoder.encode(request.password());

        // 4. User 생성
        User user = new User(
                request.nickname(),
                encodedPassword,
                request.studentNumber(),
                request.departmentId()
        );

        // 5. DB 저장
        User savedUser = userRepository.save(user);

        // 6. 사용한 이메일 인증 상태 삭제
        redisTemplate.delete(verifiedKey);

        // 7. 응답
        return new SignupResponse(
                savedUser.getId(),
                savedUser.getNickname(),
                savedUser.isOnboardingCompleted()
        );
    }
}
