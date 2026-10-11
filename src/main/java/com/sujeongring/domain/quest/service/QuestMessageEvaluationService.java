package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.enums.QuestCompletionType;
import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import com.sujeongring.domain.quest.repository.RelationshipQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestMessageEvaluationService {

    private final RelationshipQuestRepository relationshipQuestRepository;
    private final MessageCountQuestService messageCountQuestService;
    private final AiAnalysisQuestService aiAnalysisQuestService;

    @Transactional
    public Optional<QuestCompletionResult> handleTextMessage(
            ChatRoom chatRoom
    ) {
        RelationshipQuest activeQuest =
                relationshipQuestRepository
                        .findByRelationshipIdAndStatusWithQuest(
                                chatRoom.getRelationship().getId(),
                                RelationshipQuestStatus.ACTIVE
                        )
                        .orElse(null);

        if (activeQuest == null) {
            return Optional.empty();
        }

        QuestCompletionType completionType =
                activeQuest.getQuest().getCompletionType();

        return switch (completionType) {
            case MESSAGE_COUNT ->
                    messageCountQuestService.handleTextMessage(
                            chatRoom,
                            activeQuest
                    );
            case AI_ANALYSIS ->
                    aiAnalysisQuestService.handleTextMessage(
                            chatRoom,
                            activeQuest
                    );
            case BOTH_PARTICIPATION -> Optional.empty();
        };
    }
}
