
package com.sujeongring.domain.user.exception;

import com.sujeongring.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_NOT_FOUND",
            "사용자를 찾을 수 없습니다."
    ),

    ONBOARDING_ALREADY_COMPLETED(
            HttpStatus.CONFLICT,
            "ONBOARDING_ALREADY_COMPLETED",
            "이미 온보딩을 완료한 사용자입니다."
    ),

    ONBOARDING_INVALID_SELECTION(
            HttpStatus.BAD_REQUEST,
            "ONBOARDING_INVALID_SELECTION",
            "온보딩 선택값이 올바르지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}

