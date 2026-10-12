package com.sujeongring.domain.user.dto.response;

public record OnboardingCompleteResponse(
        Long userId,
        boolean onboardingCompleted
) {
}