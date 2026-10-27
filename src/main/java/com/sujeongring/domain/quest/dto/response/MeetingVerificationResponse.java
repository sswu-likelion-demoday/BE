package com.sujeongring.domain.quest.dto.response;

public record MeetingVerificationResponse(
        boolean verified,
        String partnerParticipationStatus,
        boolean questCompleted,
        boolean relationshipCompleted,
        CompletionInfo completion
) {
    public record CompletionInfo(
            Integer completedStage,
            String unlockLabel
    ) {
    }
}
