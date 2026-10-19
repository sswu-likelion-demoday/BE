package com.sujeongring.domain.quest.service;

public record MeetingVerificationCompletionResult(
        boolean partnerVerified,
        boolean questCompleted,
        boolean relationshipCompleted
) {
}
