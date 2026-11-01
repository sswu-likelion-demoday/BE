package com.sujeongring.domain.category.controller;

import com.sujeongring.domain.category.dto.request.CategoryMatchCreateRequest;
import com.sujeongring.domain.category.dto.response.CategoryMatchCreateResponse;
import com.sujeongring.domain.category.dto.response.CategoryMatchResultResponse;
import com.sujeongring.domain.category.dto.response.CategoryMatchStatusResponse;
import com.sujeongring.domain.category.service.CategoryMatchService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
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
public class CategoryMatchController {

    private final CategoryMatchService categoryMatchService;

    @GetMapping("/status")
    public ApiResponse<CategoryMatchStatusResponse> getStatus(
            @CurrentUserId Long userId
    ) {
        return ApiResponse.success(
                "카테고리 매칭 상태를 조회하였습니다.",
                categoryMatchService.getStatus(userId)
        );
    }

    @PostMapping("/requests")
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
