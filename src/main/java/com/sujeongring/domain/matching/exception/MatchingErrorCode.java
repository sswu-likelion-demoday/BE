package com.sujeongring.domain.matching.exception;

import com.sujeongring.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MatchingErrorCode implements ErrorCode {

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MATCHING_USER_NOT_FOUND",
            "존재하지 않는 사용자입니다."
    ),

    ONBOARDING_NOT_COMPLETED(
            HttpStatus.BAD_REQUEST,
            "MATCHING_ONBOARDING_NOT_COMPLETED",
            "온보딩을 완료한 사용자만 매칭할 수 있습니다."
    ),

    USER_NOT_ACTIVE(
            HttpStatus.FORBIDDEN,
            "MATCHING_USER_NOT_ACTIVE",
            "활성화된 사용자만 매칭할 수 있습니다."
    ),

    NO_MATCHING_CANDIDATE(
            HttpStatus.NOT_FOUND,
            "MATCHING_NO_CANDIDATE",
            "현재 매칭 가능한 사용자가 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
