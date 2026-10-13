package com.sujeongring.domain.quest.service;

public record AiQuestEvaluationState(
        boolean userAParticipated,
        boolean userBParticipated,
        boolean reciprocalConversation
) {

    public boolean completed() {
        return userAParticipated
                && userBParticipated
                && reciprocalConversation;
    }
}
