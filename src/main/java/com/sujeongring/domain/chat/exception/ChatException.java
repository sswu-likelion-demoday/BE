package com.sujeongring.domain.chat.exception;

import com.sujeongring.global.error.exception.BaseException;

public class ChatException extends BaseException {

    public ChatException(ChatErrorCode errorCode) {
        super(errorCode);
    }

    public ChatException(
            ChatErrorCode errorCode,
            String customMessage
    ) {
        super(errorCode, customMessage);
    }
}
