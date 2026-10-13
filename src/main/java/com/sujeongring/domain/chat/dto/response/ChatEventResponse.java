package com.sujeongring.domain.chat.dto.response;

import com.sujeongring.domain.chat.enums.ChatEventType;
import com.sujeongring.domain.quest.enums.QuestType;

public record ChatEventResponse(

        ChatEventType type,

        Long relationshipId,

        Long relationshipQuestId,

        Integer stage,

        Integer completedStage,

        QuestType questType,

        String title,

        String description,

        String unlockLabel
) {

    public static ChatEventResponse questOpened(
            Long relationshipId,
            Long relationshipQuestId,
            Integer stage,
            QuestType questType,
            String title,
            String description
    ) {
        return new ChatEventResponse(
                ChatEventType.QUEST_OPENED,
                relationshipId,
                relationshipQuestId,
                stage,
                null,
                questType,
                title,
                description,
                null
        );
    }

    public static ChatEventResponse informationUnlocked(
            Long relationshipId,
            Integer completedStage,
            String unlockLabel
    ) {
        return new ChatEventResponse(
                ChatEventType.INFORMATION_UNLOCKED,
                relationshipId,
                null,
                null,
                completedStage,
                null,
                null,
                null,
                unlockLabel
        );
    }

    public static ChatEventResponse relationshipCompleted(
            Long relationshipId
    ) {
        return new ChatEventResponse(
                ChatEventType.RELATIONSHIP_COMPLETED,
                relationshipId,
                null,
                null,
                4,
                null,
                null,
                null,
                null
        );
    }
}
