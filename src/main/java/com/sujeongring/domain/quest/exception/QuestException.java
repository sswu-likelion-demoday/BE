package com.sujeongring.domain.quest.exception;

import com.sujeongring.global.error.exception.BaseException;

public class QuestException extends BaseException {

    public QuestException(QuestErrorCode errorCode) {
        super(errorCode);
    }

    public QuestException(
            QuestErrorCode errorCode,
            String customMessage
    ) {
        super(errorCode, customMessage);
    }
}
