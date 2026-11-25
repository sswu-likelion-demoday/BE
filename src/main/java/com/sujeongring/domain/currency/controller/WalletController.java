package com.sujeongring.domain.currency.controller;

import com.sujeongring.domain.currency.dto.CurrencyHistoryResponse;
import com.sujeongring.domain.currency.enums.CurrencyTransactionType;
import com.sujeongring.domain.currency.service.WalletService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    @Operation(
            summary = "수정구 내역 조회",
            description = "EARN: 획득 내역, USE: 사용 내역"
    )
    @GetMapping("/transactions")
    public ApiResponse<CurrencyHistoryResponse> getTransactions(
            @CurrentUserId Long userId,
            @RequestParam("type") CurrencyTransactionType type
    ) {
        return ApiResponse.success(
                "수정구 내역을 조회하였습니다.",
                walletService.getCurrencyHistory(userId, type)
        );
    }
}
