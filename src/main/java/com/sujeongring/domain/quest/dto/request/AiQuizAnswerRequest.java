package com.sujeongring.domain.quest.dto.request;

import jakarta.validation.constraints.NotNull;

public record AiQuizAnswerRequest(
        @NotNull(message = "selectedOptionId는 필수입니다.") Integer selectedOptionId
) {
}
