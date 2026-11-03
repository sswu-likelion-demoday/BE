package com.sujeongring.domain.category.dto.request;

import com.sujeongring.domain.category.enums.CategoryType;
import com.sujeongring.domain.user.enums.MbtiType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CategoryMatchCreateRequest(
        @NotNull CategoryType category,
        @NotNull @Min(2) @Max(4) Integer memberCount,
        @Valid Conditions conditions,
        @NotNull Boolean allowPartialMatch
) {
    public record Conditions(
            Integer admissionYearMin,
            Integer admissionYearMax,
            String department,
            MbtiType mbti
    ) {
    }
}
