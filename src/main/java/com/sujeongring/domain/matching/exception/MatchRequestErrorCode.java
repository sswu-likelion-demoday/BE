package com.sujeongring.domain.matching.exception;

import com.sujeongring.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MatchRequestErrorCode implements ErrorCode {

    RECOMMENDATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MATCH_REQUEST_404_1",
            "추천 결과를 찾을 수 없습니다."
    ),

    RECOMMENDATION_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "MATCH_REQUEST_403_1",
            "본인의 추천 결과만 사용할 수 있습니다."
    ),

    SELF_REQUEST_NOT_ALLOWED(
            HttpStatus.BAD_REQUEST,
            "MATCH_REQUEST_400_1",
            "자기 자신에게 채팅 요청을 보낼 수 없습니다."
    ),

    DUPLICATE_MATCH_REQUEST(
            HttpStatus.CONFLICT,
            "MATCH_REQUEST_409_1",
            "이미 해당 추천 결과로 채팅 요청을 보냈습니다."
    ),

    ACTIVE_RELATIONSHIP_EXISTS(
            HttpStatus.CONFLICT,
            "MATCH_REQUEST_409_2",
            "이미 관계가 진행 중인 사용자입니다."
    ),

    BLOCKED_USER(
            HttpStatus.FORBIDDEN,
            "MATCH_REQUEST_403_2",
            "차단 관계인 사용자에게 요청할 수 없습니다."
    ),

    MATCH_REQUEST_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MATCH_REQUEST_404_2",
            "채팅 요청을 찾을 수 없습니다."
    ),

    MATCH_REQUEST_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "MATCH_REQUEST_403_3",
            "해당 채팅 요청을 조회할 권한이 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;


}
