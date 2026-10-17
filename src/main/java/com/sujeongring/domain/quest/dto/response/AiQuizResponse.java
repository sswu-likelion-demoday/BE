package com.sujeongring.domain.quest.dto.response;

import java.util.List;

public record AiQuizResponse(
        Long relationshipQuestId,
        StageInfo stage,
        String title,
        String question,
        List<Option> options
) {

    public record StageInfo(
            int stage,
            String name
    ) {
    }

    public record Option(
            int optionId,
            String content
    ) {
    }
}
