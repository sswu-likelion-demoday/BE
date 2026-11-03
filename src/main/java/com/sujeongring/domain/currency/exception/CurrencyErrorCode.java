package com.sujeongring.domain.currency.exception;

import com.sujeongring.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CurrencyErrorCode implements ErrorCode {

    WALLET_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "WALLET_NOT_FOUND",
            "사용자의 수정구 지갑을 찾을 수 없습니다."
    ),

    INSUFFICIENT_CURRENCY_BALANCE(
            HttpStatus.CONFLICT,
            "INSUFFICIENT_CURRENCY_BALANCE",
            "수정구 잔액이 부족합니다."
    ),

    INVALID_CURRENCY_AMOUNT(
            HttpStatus.BAD_REQUEST,
            "INVALID_CURRENCY_AMOUNT",
            "수정구 금액은 0보다 커야 합니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
