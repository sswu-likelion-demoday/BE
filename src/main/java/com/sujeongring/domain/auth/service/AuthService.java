package com.sujeongring.domain.auth.service;

import com.sujeongring.domain.auth.dto.request.LoginRequest;
import com.sujeongring.domain.auth.dto.request.SignupRequest;
import com.sujeongring.domain.auth.dto.request.TokenReissueRequest;
import com.sujeongring.domain.auth.dto.response.LoginResponse;
import com.sujeongring.domain.auth.dto.response.SignupResponse;
import com.sujeongring.domain.auth.dto.response.StudentNumberCheckResponse;
import com.sujeongring.domain.auth.dto.response.TokenResponse;
import com.sujeongring.domain.auth.exception.AuthErrorCode;
import com.sujeongring.domain.currency.entity.Wallet;
import com.sujeongring.domain.currency.repository.WalletRepository;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.enums.UserStatus;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.global.error.exception.BaseException;
import com.sujeongring.global.jwt.JwtTokenProvider;
import com.sujeongring.domain.auth.dto.request.PasswordResetRequest;
import com.sujeongring.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String VERIFIED_KEY_PREFIX = "email:verified:";

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final S3Service s3Service;

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
    public SignupResponse signup(
            SignupRequest request,
            MultipartFile profileImage
    ) {

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
        if (userRepository.existsByStudentNumber(
                request.studentNumber()
        )) {
            throw new BaseException(
                    AuthErrorCode.DUPLICATE_STUDENT_NUMBER
            );
        }

        // 비밀번호 암호화
        String encodedPassword =
                passwordEncoder.encode(request.password());

        /*
         * 회원가입 검증이 끝난 뒤 프로필 이미지를 S3에 업로드한다.
         *
         * 반환되는 값은 URL이 아니라
         * ex) prod/profile-images/uuid.jpg 형태의 objectKey이다.
         */
        String profileImageKey = null;

        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageKey =
                    s3Service.uploadProfileImage(profileImage);
        }

        // User 생성
        User user = new User(
                request.name(),
                request.nickname(),
                encodedPassword,
                request.studentNumber(),
                request.departmentName()
        );

        user.updateContact(
                request.contact().trim()
        );

        /*
         * profileImageUrl 컬럼에는
         * 실제 URL이 아닌 S3 objectKey를 저장한다.
         */
        if (profileImageKey != null) {
            user.updateProfileImage(profileImageKey);
        }

        User savedUser =
                userRepository.save(user);

        walletRepository.save(
                new Wallet(savedUser)
        );

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

        // 탈퇴한 사용자 로그인 제한
        if (user.getStatus() != UserStatus.ACTIVE) {
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

    /**
     * 로그아웃
     */
    public void logout(Long userId) {

        // Redis에 저장된 Refresh Token 삭제
        refreshTokenService.delete(userId);
    }
}