package com.sujeongring.domain.currency.repository;

import com.sujeongring.domain.currency.entity.CurrencyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurrencyTransactionRepository
        extends JpaRepository<CurrencyTransaction, Long> {

    List<CurrencyTransaction> findAllByWalletIdOrderByCreatedAtDesc(Long walletId);
}
