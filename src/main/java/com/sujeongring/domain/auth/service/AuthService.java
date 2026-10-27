package com.sujeongring.domain.auth.service;

import com.sujeongring.domain.auth.dto.request.LoginRequest;
import com.sujeongring.domain.auth.dto.request.SignupRequest;
import com.sujeongring.domain.auth.dto.request.TokenReissueRequest;
import com.sujeongring.domain.auth.dto.response.LoginResponse;
import com.sujeongring.domain.auth.dto.response.SignupResponse;
import com.sujeongring.domain.auth.dto.response.StudentNumberCheckResponse;
import com.sujeongring.domain.auth.dto.response.TokenResponse;
import com.sujeongring.domain.auth.exception.AuthErrorCode;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.global.error.exception.BaseException;
import com.sujeongring.global.jwt.JwtTokenProvider;
import com.sujeongring.domain.auth.dto.request.PasswordResetRequest;
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
                    AuthErrorCode.EMAIL_NOT_VERIFIED
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
                    AuthErrorCode.STUDENT_NUMBER_MISMATCH
            );
        }

        // 학번 중복 재확인
        if (userRepository.existsByStudentNumber(request.studentNumber())) {
            throw new BaseException(
                    AuthErrorCode.DUPLICATE_STUDENT_NUMBER
            );
        }

        // 비밀번호 암호화
        String encodedPassword =
                passwordEncoder.encode(request.password());

        // User 생성
        User user = new User(
                request.name(),
                request.nickname(),
                encodedPassword,
                request.studentNumber(),
                request.departmentName()
        );

        User savedUser = userRepository.save(user);

        // 이메일 인증 정보 삭제
        redisTemplate.delete(verifiedKey);

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

        User user = userRepository.findByStudentNumber(
                        request.studentNumber()
                )
                .orElseThrow(() ->
                        new BaseException(
                                AuthErrorCode.INVALID_CREDENTIALS
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new BaseException(
                    AuthErrorCode.INVALID_CREDENTIALS
            );
        }

        String accessToken =
                jwtTokenProvider.createAccessToken(user.getId());

        String refreshToken =
                jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenService.save(
                user.getId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiration()
        );

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
    public TokenResponse reissue(TokenReissueRequest request) {

        String refreshToken = request.refreshToken();

        jwtTokenProvider.validateRefreshToken(refreshToken);

        Long userId =
                jwtTokenProvider.getUserId(refreshToken);

        String savedRefreshToken =
                refreshTokenService.get(userId);

        if (savedRefreshToken == null ||
                !savedRefreshToken.equals(refreshToken)) {

            throw new BaseException(
                    AuthErrorCode.REFRESH_TOKEN_MISMATCH
            );
        }

        String newAccessToken =
                jwtTokenProvider.createAccessToken(userId);

        String newRefreshToken =
                jwtTokenProvider.createRefreshToken(userId);

        refreshTokenService.save(
                userId,
                newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpiration()
        );

        return new TokenResponse(
                newAccessToken,
                newRefreshToken
        );
    }

    /**
     * 비밀번호 재설정
     */
    @Transactional
    public void resetPassword(PasswordResetRequest request) {

        // 이메일 인증 여부 확인
        String verifiedKey =
                VERIFIED_KEY_PREFIX + request.email();

        String verified =
                redisTemplate.opsForValue().get(verifiedKey);

        if (!"true".equals(verified)) {
            throw new BaseException(
                    AuthErrorCode.EMAIL_NOT_VERIFIED
            );
        }

        // 이메일의 학번과 입력한 학번이 같은지 확인
        String emailStudentNumber =
                request.email().substring(
                        0,
                        request.email().indexOf("@")
                );

        if (!emailStudentNumber.equals(request.studentNumber())) {
            throw new BaseException(
                    AuthErrorCode.STUDENT_NUMBER_MISMATCH
            );
        }

        // 가입된 사용자 조회
        User user = userRepository
                .findByStudentNumber(request.studentNumber())
                .orElseThrow(() ->
                        new BaseException(
                                AuthErrorCode.USER_NOT_FOUND
                        )
                );

        // 새 비밀번호 암호화
        String encodedPassword =
                passwordEncoder.encode(request.newPassword());

        // 비밀번호 변경
        user.changePassword(encodedPassword);

        // 인증 정보 삭제
        redisTemplate.delete(verifiedKey);
    }
}