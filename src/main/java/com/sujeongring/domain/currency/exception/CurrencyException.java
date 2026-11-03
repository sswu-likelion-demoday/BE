package com.sujeongring.domain.currency.exception;

import com.sujeongring.global.error.exception.BaseException;

public class CurrencyException extends BaseException {

    public CurrencyException(CurrencyErrorCode errorCode) {
        super(errorCode);
    }

    public CurrencyException(
            CurrencyErrorCode errorCode,
            String customMessage
    ) {
        super(errorCode, customMessage);
    }
}
