package com.sujeongring.domain.currency.repository;

import com.sujeongring.domain.currency.entity.CurrencyTransaction;
import com.sujeongring.domain.currency.enums.CurrencyTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurrencyTransactionRepository
        extends JpaRepository<CurrencyTransaction, Long> {

    List<CurrencyTransaction>
    findAllByWalletIdAndTransactionTypeOrderByCreatedAtDescIdDesc(
            Long walletId,
            CurrencyTransactionType transactionType
    );
}
