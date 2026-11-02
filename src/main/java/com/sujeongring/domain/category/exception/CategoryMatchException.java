package com.sujeongring.domain.category.exception;

import com.sujeongring.global.error.exception.BaseException;

public class CategoryMatchException extends BaseException {

    public CategoryMatchException(CategoryMatchErrorCode errorCode) {
        super(errorCode);
    }

    public CategoryMatchException(
            CategoryMatchErrorCode errorCode,
            String customMessage
    ) {
        super(errorCode, customMessage);
    }
}
