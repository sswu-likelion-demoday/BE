package com.sujeongring.domain.matching.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateMatchRequestRequest(
        @NotNull(message = "추천 ID는 필수입니다.")
        @Positive(message = "추천 ID는 양수여야 합니다.")
        Long recommendationId
) {
}
