package com.sujeongring.domain.quest.dto.response;

import java.util.List;

public record AiQuizGenerationResult(
        String question,
        List<Option> options,
        int correctOption
) {

    public record Option(
            int optionId,
            String content
    ) {
    }
}
