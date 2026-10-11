package com.sujeongring.domain.relationship.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RelationshipProgressResponse(

        Long relationshipId,
        RelationshipStatus relationshipStatus,

        Partner partner,

        CurrentStage currentStage,
        int totalStages,

        List<StageItem> stages
) {

    public record Partner(
            Long userId,
            String nickname,
            String profileImageUrl,
            boolean profileImageUnlocked
    ) {
    }

    public record CurrentStage(
            int stage,
            String name
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StageItem(
            int stage,
            String name,
            StageStatus status,
            QuestProgress questProgress,
            List<QuestItem> quests,
            Unlock unlock
    ) {
    }

    public record QuestProgress(
            int completed,
            int total
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record QuestItem(
            Long relationshipQuestId,
            String title,
            QuestResponseType type,
            RelationshipQuestStatus status,

            String myParticipationStatus,
            String partnerParticipationStatus
    ) {
    }

    public record Unlock(
            UnlockStatus status,
            String label
    ) {
    }

    public enum StageStatus {
        COMPLETED,
        ACTIVE,
        LOCKED
    }

    public enum UnlockStatus {
        UNLOCKED,
        LOCKED
    }
}
