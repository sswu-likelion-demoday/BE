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
    ),

    RELATIONSHIP_NOT_ACTIVE(
            HttpStatus.CONFLICT,
            "RELATIONSHIP_NOT_ACTIVE",
            "현재 활성 상태의 관계만 종료할 수 있습니다."
    ),

    RELATIONSHIP_BLOCK_END_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "RELATIONSHIP_BLOCK_END_NOT_ALLOWED",
            "차단을 통한 관계 종료는 ACTIVE 또는 COMPLETED 관계에서만 할 수 있습니다."
    ),

    MATCH_SLOT_FULL(
            HttpStatus.CONFLICT,
            "MATCH_SLOT_FULL",
            "활성 관계 슬롯이 가득 찼습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
