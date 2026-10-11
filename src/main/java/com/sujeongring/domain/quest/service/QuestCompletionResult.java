package com.sujeongring.domain.quest.service;

public record QuestCompletionResult(

        Long relationshipId,

        Long completedQuestId,

        int completedStage,

        Long openedQuestId,

        Integer newStage,

        boolean stageCompleted,

        boolean relationshipCompleted
) {

    public static QuestCompletionResult nextQuestOpened(
            Long relationshipId,
            int completedStage,
            Long completedQuestId,
            Long openedQuestId
    ) {
        return new QuestCompletionResult(
                relationshipId,
                completedQuestId,
                completedStage,
                openedQuestId,
                null,
                false,
                false
        );
    }

    public static QuestCompletionResult stageCompleted(
            Long relationshipId,
            int completedStage,
            int newStage,
            Long completedQuestId,
            Long openedQuestId
    ) {
        return new QuestCompletionResult(
                relationshipId,
                completedQuestId,
                completedStage,
                openedQuestId,
                newStage,
                true,
                false
        );
    }

    public static QuestCompletionResult relationshipCompleted(
            Long relationshipId,
            int completedStage,
            Long completedQuestId
    ) {
        return new QuestCompletionResult(
                relationshipId,
                completedQuestId,
                completedStage,
                null,
                null,
                true,
                true
        );
    }
}
