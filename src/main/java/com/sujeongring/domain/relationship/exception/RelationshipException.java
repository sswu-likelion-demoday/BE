package com.sujeongring.domain.relationship.exception;

import com.sujeongring.global.error.exception.BaseException;

public class RelationshipException extends BaseException {

    public RelationshipException(RelationshipErrorCode errorCode) {
        super(errorCode);
    }

    public RelationshipException(
            RelationshipErrorCode errorCode,
            String customMessage
    ) {
        super(errorCode, customMessage);
    }
}
