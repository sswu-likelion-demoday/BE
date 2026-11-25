package com.sujeongring.domain.currency.service;

import com.sujeongring.domain.currency.dto.CurrencyHistoryResponse;
import com.sujeongring.domain.currency.entity.CurrencyTransaction;
import com.sujeongring.domain.currency.entity.Wallet;
import com.sujeongring.domain.currency.enums.CurrencyTransactionType;
import com.sujeongring.domain.currency.repository.CurrencyTransactionRepository;
import com.sujeongring.domain.currency.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {

    private final WalletRepository walletRepository;
    private final CurrencyTransactionRepository
            currencyTransactionRepository;

    // 수정구 획득 및 사용 내역 조회
    public CurrencyHistoryResponse getCurrencyHistory(
            Long userId,
            CurrencyTransactionType type
    ) {

        // 1. 로그인한 사용자의 지갑 조회
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElse(null);

// 지갑이 없는 경우 잔액 0 및 빈 거래 내역 반환
        if (wallet == null) {
            return new CurrencyHistoryResponse(
                    0,
                    type,
                    List.of()
            );
        }
        // 2. EARN 또는 USE 거래 내역 조회
        List<CurrencyTransaction> transactions =
                currencyTransactionRepository
                        .findAllByWalletIdAndTransactionTypeOrderByCreatedAtDescIdDesc(
                                wallet.getId(),
                                type
                        );

        // 3. 거래 날짜별 그룹화
        Map<LocalDate,
                List<CurrencyHistoryResponse.TransactionHistory>>
                grouped = new LinkedHashMap<>();

        for (CurrencyTransaction transaction : transactions) {

            LocalDate date = transaction.getCreatedAt()
                    .toLocalDate();

            grouped.computeIfAbsent(
                    date,
                    key -> new ArrayList<>()
            ).add(toTransactionHistory(transaction));
        }

        // 4. 날짜별 응답 DTO 생성
        List<CurrencyHistoryResponse.DailyHistory> histories =
                grouped.entrySet().stream()
                        .map(entry ->
                                new CurrencyHistoryResponse.DailyHistory(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        // 5. 현재 보유 수정구 및 거래 내역 반환
        return new CurrencyHistoryResponse(
                wallet.getBalance(),
                type,
                histories
        );
    }

    // 개별 거래 내역 DTO 변환
    private CurrencyHistoryResponse.TransactionHistory
    toTransactionHistory(CurrencyTransaction transaction) {

        // 획득은 양수, 사용은 음수
        int amount =
                transaction.getTransactionType()
                        == CurrencyTransactionType.USE
                        ? -Math.abs(transaction.getAmount())
                        : Math.abs(transaction.getAmount());

        return new CurrencyHistoryResponse.TransactionHistory(
                transaction.getId(),
                transaction.getReason(),
                transaction.getReason().getTitle(),
                getDescription(transaction),
                amount,
                transaction.getCreatedAt()
        );
    }

    // 상세 거래 사유 반환
    private String getDescription(
            CurrencyTransaction transaction
    ) {

        // 저장된 상세 거래 사유가 존재하는 경우
        if (transaction.getDescription() != null
                && !transaction.getDescription().isBlank()) {

            return transaction.getDescription();
        }

        // 기존 거래 데이터에 대한 기본 설명
        return switch (transaction.getReason()) {

            case QUEST_REWARD ->
                    "퀘스트 완료 보상";

            case RELATIONSHIP_REWARD ->
                    "관계 단계 달성 보상";


            case EXTRA_MATCH ->
                    "추가 매칭 이용";

            default ->
                    transaction.getReason().getTitle();
        };
    }
}