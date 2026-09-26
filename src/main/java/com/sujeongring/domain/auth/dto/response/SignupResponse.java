package com.sujeongring.domain.auth.dto.response;

public record SignupResponse(
        Long userId,
        String nickname,
        boolean onboardingCompleted
) {
}