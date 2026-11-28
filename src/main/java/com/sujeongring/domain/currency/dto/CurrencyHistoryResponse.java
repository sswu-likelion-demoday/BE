package com.sujeongring.domain.currency.dto;

import com.sujeongring.domain.currency.enums.CurrencyReason;
import com.sujeongring.domain.currency.enums.CurrencyTransactionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CurrencyHistoryResponse(
        int balance,
        CurrencyTransactionType type,
        List<DailyHistory> transactions
) {

    public record DailyHistory(
            LocalDate date,
            List<TransactionHistory> transactions
    ) {}

    public record TransactionHistory(
            Long transactionId,
            CurrencyReason reason,
            String title,
            String description,
            int amount,
            LocalDateTime createdAt
    ) {}
}