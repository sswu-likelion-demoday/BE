package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.chat.exception.ChatErrorCode;
import com.sujeongring.domain.chat.exception.ChatException;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.enums.QuestCompletionType;
import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import com.sujeongring.domain.quest.repository.RelationshipQuestRepository;
import com.sujeongring.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestMessageEvaluationService {

    private final ChatRoomRepository chatRoomRepository;
    private final RelationshipQuestRepository relationshipQuestRepository;
    private final MessageCountQuestService messageCountQuestService;
    private final AiAnalysisQuestService aiAnalysisQuestService;

    public Optional<QuestCompletionResult> handleTextMessage(
            Long chatRoomId,
            Long triggerMessageId
    ) {
        ChatRoom chatRoom =
                chatRoomRepository
                        .findDetailById(chatRoomId)
                        .orElseThrow(() ->
                                new ChatException(
                                        ChatErrorCode.CHAT_ROOM_NOT_FOUND
                                )
                        );

        RelationshipQuest activeQuest =
                relationshipQuestRepository
                        .findByRelationshipIdAndStatusWithQuest(
                                chatRoom.getRelationship().getId(),
                                RelationshipQuestStatus.ACTIVE
                        )
                        .orElse(null);

        if (activeQuest == null) {
            log.info(
                    "[QUEST] active quest not found - chatRoomId={}",
                    chatRoomId
            );
            return Optional.empty();
        }

        QuestCompletionType completionType =
                activeQuest.getQuest().getCompletionType();

        log.info(
                "[QUEST] evaluating - questId={}, title={}, type={}",
                activeQuest.getId(),
                activeQuest.getQuest().getTitle(),
                completionType
        );

        return switch (completionType) {
            case MESSAGE_COUNT ->
                    messageCountQuestService.handleTextMessage(
                            chatRoom,
                            activeQuest,
                            triggerMessageId
                    );
            case AI_ANALYSIS ->
                    aiAnalysisQuestService.handleTextMessage(
                            chatRoom,
                            activeQuest,
                            triggerMessageId
                    );
            case BOTH_PARTICIPATION -> Optional.empty();
        };
    }
}
