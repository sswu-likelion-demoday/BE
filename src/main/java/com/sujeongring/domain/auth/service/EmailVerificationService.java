package com.sujeongring.domain.auth.service;

import com.sujeongring.domain.auth.dto.response.EmailSendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.sujeongring.domain.auth.dto.response.EmailVerifyResponse;
import com.sujeongring.global.error.ErrorCode;
import com.sujeongring.global.error.exception.BaseException;
import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int EXPIRES_IN = 300;
    private static final String KEY_PREFIX = "email:verification:";
    private static final String VERIFIED_KEY_PREFIX = "email:verified:";
    private final SecureRandom secureRandom = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    private String from;
    public EmailSendResponse sendVerificationCode(String email) {

        String verificationCode = generateVerificationCode();

        saveVerificationCode(email, verificationCode);

        sendEmail(email, verificationCode);

        return new EmailSendResponse(EXPIRES_IN);
    }

    private String generateVerificationCode() {
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private void saveVerificationCode(
            String email,
            String verificationCode
    ) {
        String key = KEY_PREFIX + email;

        redisTemplate.opsForValue().set(
                key,
                verificationCode,
                Duration.ofSeconds(EXPIRES_IN)
        );
    }

    private void sendEmail(
            String email,
            String verificationCode
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[수정구함] 이메일 인증번호");
        message.setText(
                "수정구함 이메일 인증번호입니다.\n\n"
                        + "인증번호: " + verificationCode
                        + "\n\n인증번호는 5분 동안 유효합니다."
        );

        mailSender.send(message);
    }

    public EmailVerifyResponse verifyCode(
            String email,
            String inputCode
    ) {
        String verificationKey = KEY_PREFIX + email;

        String savedCode = redisTemplate.opsForValue().get(verificationKey);

        // 인증번호가 존재하지 않음 = 만료
        if (savedCode == null) {
            throw new BaseException(
                    ErrorCode.AUTH_VERIFICATION_CODE_EXPIRED
            );
        }

        // 인증번호 불일치
        if (!savedCode.equals(inputCode)) {
            throw new BaseException(
                    ErrorCode.AUTH_INVALID_VERIFICATION_CODE
            );
        }

        // 인증번호 사용 완료
        redisTemplate.delete(verificationKey);

        // 이메일 인증 완료 상태 저장
        String verifiedKey = VERIFIED_KEY_PREFIX + email;

        redisTemplate.opsForValue().set(
                verifiedKey,
                "true",
                Duration.ofMinutes(30)
        );

        return new EmailVerifyResponse(true);
    }
}