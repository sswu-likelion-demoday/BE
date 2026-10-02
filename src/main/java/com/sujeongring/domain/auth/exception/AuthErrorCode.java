package com.sujeongring.domain.auth.exception;


import com.sujeongring.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 이메일 인증
    INVALID_VERIFICATION_CODE(
            HttpStatus.BAD_REQUEST,
            "AUTH_INVALID_VERIFICATION_CODE",
            "인증번호가 일치하지 않습니다."
    ),

    VERIFICATION_CODE_EXPIRED(
            HttpStatus.BAD_REQUEST,
            "AUTH_VERIFICATION_CODE_EXPIRED",
            "인증번호가 만료되었습니다."
    ),

    EMAIL_NOT_VERIFIED(
            HttpStatus.BAD_REQUEST,
            "AUTH_EMAIL_NOT_VERIFIED",
            "이메일 인증이 필요합니다."
    ),

    STUDENT_NUMBER_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "AUTH_STUDENT_NUMBER_MISMATCH",
            "이메일과 학번이 일치하지 않습니다."
    ),

    //회원가입
    DUPLICATE_STUDENT_NUMBER(
            HttpStatus.CONFLICT,
            "AUTH_DUPLICATE_STUDENT_NUMBER",
            "이미 가입된 학번입니다."
    ),

    //로그인
    INVALID_LOGIN_REQUEST(
            HttpStatus.BAD_REQUEST,
            "AUTH_INVALID_LOGIN_REQUEST",
            "학번과 비밀번호를 입력해주세요."
    ),

    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "AUTH_INVALID_CREDENTIALS",
            "학번 또는 비밀번호가 올바르지 않습니다."
    ),

    // JWT
    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_INVALID_REFRESH_TOKEN",
            "유효하지 않은 Refresh Token입니다."
    ),

    EXPIRED_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_EXPIRED_REFRESH_TOKEN",
            "만료된 Refresh Token입니다."
    ),

    REFRESH_TOKEN_MISMATCH(
            HttpStatus.UNAUTHORIZED,
            "AUTH_REFRESH_TOKEN_MISMATCH",
            "Refresh Token이 일치하지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}