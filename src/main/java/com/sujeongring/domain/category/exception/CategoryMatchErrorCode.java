package com.sujeongring.domain.category.exception;

import com.sujeongring.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryMatchErrorCode implements ErrorCode {

    CATEGORY_MATCH_REQUEST_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CATEGORY_MATCH_REQUEST_NOT_FOUND",
            "카테고리 매칭 신청을 찾을 수 없습니다."
    ),

    CATEGORY_MATCH_ALREADY_ACTIVE(
            HttpStatus.CONFLICT,
            "CATEGORY_MATCH_ALREADY_ACTIVE",
            "해당 카테고리에 진행 중인 매칭이 있어 새로 신청할 수 없습니다."
    ),

    CATEGORY_MATCH_CONTACT_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "CATEGORY_MATCH_CONTACT_REQUIRED",
            "외부 연락 수단을 등록한 사용자만 신청할 수 있습니다."
    ),

    CATEGORY_MATCH_INVALID_CONDITION(
            HttpStatus.BAD_REQUEST,
            "CATEGORY_MATCH_INVALID_CONDITION",
            "카테고리 매칭 조건이 올바르지 않습니다."
    ),

    CATEGORY_MATCH_RESULT_NOT_READY(
            HttpStatus.CONFLICT,
            "CATEGORY_MATCH_RESULT_NOT_READY",
            "아직 확인 가능한 카테고리 매칭 결과가 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
