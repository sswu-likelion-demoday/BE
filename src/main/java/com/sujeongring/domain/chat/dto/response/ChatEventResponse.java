package com.sujeongring.domain.chat.dto.response;

import com.sujeongring.domain.chat.enums.ChatEventType;

public record ChatEventResponse(

        ChatEventType type,

        Long relationshipId,

        Long relationshipQuestId,

        Integer stage,

        String title,

        String description,

        String unlockLabel
) {

    public static ChatEventResponse questOpened(
            Long relationshipId,
            Long relationshipQuestId,
            Integer stage,
            String title,
            String description
    ) {
        return new ChatEventResponse(
                ChatEventType.QUEST_OPENED,
                relationshipId,
                relationshipQuestId,
                stage,
                title,
                description,
                null
        );
    }

    public static ChatEventResponse informationUnlocked(
            Long relationshipId,
            Integer stage,
            String unlockLabel
    ) {
        return new ChatEventResponse(
                ChatEventType.INFORMATION_UNLOCKED,
                relationshipId,
                null,
                stage,
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
                4,
                null,
                null,
                null
        );
    }
}
