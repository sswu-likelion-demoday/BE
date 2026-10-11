package com.sujeongring.domain.relationship.exception;

import com.sujeongring.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RelationshipErrorCode implements ErrorCode {

    RELATIONSHIP_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RELATIONSHIP_NOT_FOUND",
            "관계를 찾을 수 없습니다."
    ),

    RELATIONSHIP_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "RELATIONSHIP_ACCESS_DENIED",
            "해당 관계에 접근할 권한이 없습니다."
    ),

    RELATIONSHIP_ENDED(
            HttpStatus.CONFLICT,
            "RELATIONSHIP_ENDED",
            "종료된 관계입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
