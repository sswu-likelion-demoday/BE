package com.sujeongring.domain.user.controller;

import com.sujeongring.domain.user.dto.request.OnboardingCompleteRequest;
import com.sujeongring.domain.user.dto.response.OnboardingCompleteResponse;
import com.sujeongring.domain.user.service.OnboardingService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/complete")
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
