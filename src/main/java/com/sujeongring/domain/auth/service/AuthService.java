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
import com.sujeongring.domain.auth.dto.request.LoginRequest;
import com.sujeongring.domain.auth.dto.response.LoginResponse;


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
     * 로그인
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        // 학번 / 비밀번호 입력 확인
        if (request.studentId() == null ||
                request.studentId().isBlank() ||
                request.password() == null ||
                request.password().isBlank()) {

            throw new BaseException(
                    AuthErrorCode.INVALID_LOGIN_REQUEST
            );
        }

        // 학번으로 사용자 조회
        User user = userRepository.findByStudentId(request.studentId())
                .orElseThrow(() ->
                        new BaseException(
                                AuthErrorCode.INVALID_CREDENTIALS
                        )
                );

        // 비밀번호 확인
        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new BaseException(
                    AuthErrorCode.INVALID_CREDENTIALS
            );
        }

        // Access Token 발급
        String accessToken =
                jwtTokenProvider.createAccessToken(user.getId());

        // Refresh Token 발급
        String refreshToken =
                jwtTokenProvider.createRefreshToken(user.getId());

        // Refresh Token Redis 저장
        refreshTokenService.save(
                user.getId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiration()
        );

        // 로그인 결과 반환
        return new LoginResponse(
                user.getId(),
                user.getNickname(),
                accessToken,
                refreshToken,
                user.isOnboardingCompleted()
        );
    }


    /**
     * Access Token / Refresh Token 재발급
     */
    @Transactional(readOnly = true)
    public TokenResponse reissue(TokenReissueRequest request) {

        String refreshToken = request.refreshToken();

        // Refresh Token 유효성 및 만료 여부 검증
        jwtTokenProvider.validateRefreshToken(refreshToken);

        // Refresh Token에서 userId 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // Redis에 저장된 Refresh Token 조회
        String savedRefreshToken =
                refreshTokenService.get(userId);

        // 저장된 Refresh Token 존재 여부 및 일치 여부 확인
        if (savedRefreshToken == null ||
                !savedRefreshToken.equals(refreshToken)) {

            throw new BaseException(
                    AuthErrorCode.REFRESH_TOKEN_MISMATCH
            );
        }

        // 새로운 Access Token 발급
        String newAccessToken =
                jwtTokenProvider.createAccessToken(userId);

        // 새로운 Refresh Token 발급
        String newRefreshToken =
                jwtTokenProvider.createRefreshToken(userId);

        // 새로운 Refresh Token으로 교체
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