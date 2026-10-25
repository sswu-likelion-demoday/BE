package com.sujeongring.domain.matching.dto.response;

import java.time.LocalDate;

public record DailyFreeMatchingStatusResponse(
        LocalDate date,
        int dailyFreeLimit,
        long freeUsedToday,
        int freeRemainingToday
) {}
