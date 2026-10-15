package com.sujeongring.domain.quest.dto.response;

public record AiQuizAnswerResponse(
        boolean correct,
        AiQuizResponse.Option correctAnswer,
        int reward,
        String partnerParticipationStatus,
        boolean questCompleted,
        CompletionInfo completion
) {
    public record CompletionInfo(
            boolean stageCompleted,
            Integer completedStage,
            Integer newStage,
            String unlockLabel
    ) {
    }
}
