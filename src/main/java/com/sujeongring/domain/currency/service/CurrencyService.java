
package com.sujeongring.domain.currency.service;

import com.sujeongring.domain.currency.entity.CurrencyTransaction;
import com.sujeongring.domain.currency.entity.Wallet;
import com.sujeongring.domain.currency.enums.CurrencyReason;
import com.sujeongring.domain.currency.enums.CurrencyTransactionType;
import com.sujeongring.domain.currency.repository.CurrencyTransactionRepository;
import com.sujeongring.domain.currency.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private static final int EXTRA_MATCH_COST = 1;
    private static final ZoneId KOREA_ZONE =
            ZoneId.of("Asia/Seoul");

    private final WalletRepository walletRepository;
    private final CurrencyTransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void useForExtraMatching(Long userId) {

        // 1. 지갑 조회 및 잠금
        Wallet wallet = walletRepository
                .findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "사용자의 수정구 지갑이 존재하지 않습니다."
                ));

        // 2. 오늘 날짜 확인
        LocalDate today = LocalDate.now(KOREA_ZONE);

        // 3. 오늘 초기화하지 않았다면 잔액을 3개로 설정
        if (!today.equals(wallet.getLastDailyResetDate())) {

            wallet.resetDailyBalance(today);

            transactionRepository.save(
                    new CurrencyTransaction(
                            wallet,
                            CurrencyTransactionType.RESET,
                            CurrencyReason.DAILY_RESET,
                            3,
                            wallet.getBalance()
                    )
            );
        }

        // 4. 추가 매칭 비용 1개 차감
        wallet.deduct(EXTRA_MATCH_COST);

        // 5. 차감 거래 기록 저장
        transactionRepository.save(
                new CurrencyTransaction(
                        wallet,
                        CurrencyTransactionType.USE,
                        CurrencyReason.EXTRA_MATCH,
                        EXTRA_MATCH_COST,
                        wallet.getBalance()
                )
        );
    }
}
