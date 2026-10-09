package com.sujeongring.domain.chat.dto.response;

import com.sujeongring.domain.quest.enums.QuestType;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;

public record ChatRoomDetailResponse(
        Long chatRoomId,
        Long relationshipId,
        Partner partner,
        RelationshipStatus relationshipStatus,
        boolean writable,
        Stage currentStage,
        QuestProgress questProgress,
        CurrentQuest currentQuest,
        String unlockGuideMessage
) {

    public record Partner(
            Long userId,
            String nickname,
            String profileImageUrl,
            boolean profileImageUnlocked
    ) {
    }

    public record Stage(
            int stage,
            String name
    ) {
    }

    public record QuestProgress(
            int completed,
            int total
    ) {
    }

    public record CurrentQuest(
            Long relationshipQuestId,
            String title,
            QuestType type
    ) {
    }
}
