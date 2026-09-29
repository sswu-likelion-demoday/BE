package com.sujeongring.domain.auth.service;

import com.sujeongring.domain.auth.dto.request.SignupRequest;
import com.sujeongring.domain.auth.dto.response.SignupResponse;
import com.sujeongring.domain.auth.dto.response.StudentNumberCheckResponse;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.domain.auth.exception.AuthErrorCode;
import com.sujeongring.global.error.exception.BaseException;
import com.sujeongring.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import com.sujeongring.domain.auth.dto.request.TokenReissueRequest;
import com.sujeongring.domain.auth.dto.response.TokenResponse;


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
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

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

        // 이메일 인증 여부 확인

        String verifiedKey =
                VERIFIED_KEY_PREFIX + request.email();

        String verified =
                redisTemplate.opsForValue().get(verifiedKey);

        if (!"true".equals(verified)) {
            throw new BaseException(AuthErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 이메일에 포함된 학번과 요청 학번 일치 확인
        String emailStudentNumber =
                request.email().substring(
                        0,
                        request.email().indexOf("@")
                );

        if (!emailStudentNumber.equals(request.studentNumber())) {
            throw new BaseException(AuthErrorCode.STUDENT_NUMBER_MISMATCH);
        }

        // 학번 중복 재확인
        if (userRepository.existsByStudentId(request.studentNumber())) {
            throw new BaseException(AuthErrorCode.DUPLICATE_STUDENT_NUMBER);
        }

        // 비밀번호 암호화
        String encodedPassword =
                passwordEncoder.encode(request.password());

        // User 생성
        User user = new User(
                request.nickname(),
                encodedPassword,
                request.studentNumber(),
                request.departmentId()
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


    /**
     * Access Token / Refresh Token 재발급
     */
    @Transactional(readOnly = true)
    public TokenResponse reissue(TokenReissueRequest request) {

        String refreshToken = request.refreshToken();

        // 1. Refresh Token 유효성 및 만료 여부 검증
        jwtTokenProvider.validateRefreshToken(refreshToken);

        // 2. Refresh Token에서 userId 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // 3. Redis에 저장된 Refresh Token 조회
        String savedRefreshToken =
                refreshTokenService.get(userId);

        // 4. 저장된 Refresh Token 존재 여부 및 일치 여부 확인
        if (savedRefreshToken == null ||
                !savedRefreshToken.equals(refreshToken)) {

            throw new BaseException(
                    AuthErrorCode.REFRESH_TOKEN_MISMATCH
            );
        }

        // 5. 새로운 Access Token 발급
        String newAccessToken =
                jwtTokenProvider.createAccessToken(userId);

        // 6. 새로운 Refresh Token 발급
        String newRefreshToken =
                jwtTokenProvider.createRefreshToken(userId);

        // 7. 새로운 Refresh Token으로 교체
        refreshTokenService.save(
                userId,
                newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpiration()
        );

        // 8. 새 토큰 반환
        return new TokenResponse(
                newAccessToken,
                newRefreshToken
        );
    }
}