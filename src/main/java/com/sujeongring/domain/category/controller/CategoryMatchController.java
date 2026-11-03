package com.sujeongring.domain.category.controller;

import com.sujeongring.domain.category.dto.request.CategoryMatchCreateRequest;
import com.sujeongring.domain.category.dto.response.CategoryMatchCreateResponse;
import com.sujeongring.domain.category.dto.response.CategoryMatchResultResponse;
import com.sujeongring.domain.category.dto.response.CategoryMatchStatusResponse;
import com.sujeongring.domain.category.service.CategoryMatchService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category-matches")
@Tag(name = "카테고리 매칭", description = "카테고리 매칭 신청/상태/결과 조회 및 확인 API")
public class CategoryMatchController {

    private final CategoryMatchService categoryMatchService;

    @GetMapping("/status")
    @Operation(summary = "카테고리 매칭 상태 조회", description = "카테고리별 신청 가능/대기/결과 대기 상태를 조회합니다.")
    public ApiResponse<CategoryMatchStatusResponse> getStatus(
            @CurrentUserId Long userId
    ) {
        return ApiResponse.success(
                "카테고리 매칭 상태를 조회하였습니다.",
                categoryMatchService.getStatus(userId)
        );
    }

    @PostMapping("/requests")
    @Operation(summary = "카테고리 매칭 신청", description = "선택한 카테고리와 조건으로 매칭을 신청합니다.")
    public ApiResponse<CategoryMatchCreateResponse> createRequest(
            @CurrentUserId Long userId,
            @Valid @RequestBody CategoryMatchCreateRequest request
    ) {
        return ApiResponse.success(
                "카테고리 매칭을 신청하였습니다.",
                categoryMatchService.create(userId, request)
        );
    }

    @GetMapping("/requests/{requestId}/result")
    @Operation(summary = "카테고리 매칭 결과 조회", description = "매칭 완료된 요청의 상대 목록을 조회합니다.")
    public ApiResponse<CategoryMatchResultResponse> getResult(
            @CurrentUserId Long userId,
            @PathVariable Long requestId
    ) {
        return ApiResponse.success(
                "카테고리 매칭 결과를 조회하였습니다.",
                categoryMatchService.getResult(userId, requestId)
        );
    }

    @PostMapping("/requests/{requestId}/confirm")
    @Operation(summary = "카테고리 매칭 결과 확인", description = "매칭 결과 확인 완료를 기록하고 새로운 신청이 가능하게 합니다.")
    public ApiResponse<Void> confirmResult(
            @CurrentUserId Long userId,
            @PathVariable Long requestId
    ) {
        categoryMatchService.confirmResult(userId, requestId);
        return ApiResponse.success(
                "카테고리 매칭 결과를 확인하였습니다.",
                null
        );
    }
}
