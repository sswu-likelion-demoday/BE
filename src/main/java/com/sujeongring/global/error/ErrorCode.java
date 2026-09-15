package com.sujeongring.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400_1", "잘못된 입력값입니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON_400_2", "요청 본문을 확인해주세요."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_400_3", "필수 요청 파라미터가 누락되었습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400_4", "요청 값의 타입이 올바르지 않습니다."),

    // 404 Not Found
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404_1", "요청한 리소스를 찾을 수 없습니다."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405_1", "지원하지 않는 HTTP 메서드입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_1", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}