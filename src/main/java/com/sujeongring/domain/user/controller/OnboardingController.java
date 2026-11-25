package com.sujeongring.domain.user.controller;

import com.sujeongring.domain.user.dto.request.OnboardingCompleteRequest;
import com.sujeongring.domain.user.dto.response.OnboardingCompleteResponse;
import com.sujeongring.domain.user.service.OnboardingService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboarding")
@Tag(name = "온보딩", description = "온보딩 완료 및 초기 성향 정보 저장 API")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/complete")
    @Operation(summary = "온보딩 완료", description = "MBTI/관심사/취미/친구 유형을 저장하고 온보딩을 완료합니다.")
    public ApiResponse<OnboardingCompleteResponse> complete(
            @CurrentUserId Long userId,
            @Valid @RequestBody OnboardingCompleteRequest request
    ) {
        OnboardingCompleteResponse response =
                onboardingService.complete(userId, request);

        return ApiResponse.success(
                "온보딩이 완료되었습니다.",
                response
        );
    }
}
