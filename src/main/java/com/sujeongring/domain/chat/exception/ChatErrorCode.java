package com.sujeongring.domain.chat.exception;

import com.sujeongring.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

    CHAT_ROOM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CHAT_ROOM_NOT_FOUND",
            "채팅방을 찾을 수 없습니다."
    ),

    CHAT_ROOM_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "CHAT_ROOM_ACCESS_DENIED",
            "해당 채팅방에 접근할 수 없습니다."
    ),

    CHAT_ROOM_NOT_WRITABLE(
            HttpStatus.CONFLICT,
            "CHAT_ROOM_NOT_WRITABLE",
            "현재 메시지를 전송할 수 없는 채팅방입니다."
    ),

    CHAT_MESSAGE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CHAT_MESSAGE_NOT_FOUND",
            "메시지를 찾을 수 없습니다."
    ),

    CHAT_ROOM_ENDED(
            HttpStatus.CONFLICT,
        "CHAT_ROOM_ENDED",
                "종료된 관계의 채팅방에는 접근할 수 없습니다."
    ),

    INVALID_MESSAGE_PAGE_SIZE(
            HttpStatus.BAD_REQUEST,
            "INVALID_MESSAGE_PAGE_SIZE",
            "메시지 조회 개수는 1개 이상 100개 이하여야 합니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
