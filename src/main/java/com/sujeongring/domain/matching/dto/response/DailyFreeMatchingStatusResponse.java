package com.sujeongring.domain.matching.dto.response;

public record DailyFreeMatchingStatusResponse(
        WeeklyMatching weeklyMatching,
        Currency currency,
        String actionType
) {
    public record WeeklyMatching(
            int remaining,
            int total
    ) {}

    public record Currency(
            int balance,
            int matchingCost
    ) {}
}