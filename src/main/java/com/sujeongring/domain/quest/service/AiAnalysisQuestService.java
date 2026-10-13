package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.chat.entity.ChatMessage;
import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.chat.enums.MessageType;
import com.sujeongring.domain.chat.repository.ChatMessageRepository;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.enums.QuestCompletionType;
import com.sujeongring.domain.relationship.entity.Relationship;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisQuestService {

    private static final int MIN_TOTAL_MESSAGES = 8;
    private static final int MIN_MESSAGES_PER_USER = 1;
    private static final int RECHECK_INTERVAL = 2;

    private final ChatMessageRepository chatMessageRepository;
    private final AiQuestEvaluator aiQuestEvaluator;
    private final QuestProgressService questProgressService;

    public Optional<QuestCompletionResult> handleTextMessage(
            ChatRoom chatRoom,
            RelationshipQuest activeQuest,
            Long triggerMessageId
    ) {
        if (activeQuest.getQuest().getCompletionType()
                != QuestCompletionType.AI_ANALYSIS) {
            return Optional.empty();
        }

        if (activeQuest.getOpenedAt() == null) {
            return Optional.empty();
        }

        Relationship relationship =
                chatRoom.getRelationship();

        long totalMessageCount =
                chatMessageRepository
                        .countMessagesAfterQuestOpenedUntilMessage(
                        chatRoom.getId(),
                        MessageType.TEXT,
                        activeQuest.getOpenedAt(),
                        triggerMessageId
                );

        long userAMessageCount =
                chatMessageRepository
                        .countUserMessagesAfterQuestOpenedUntilMessage(
                        chatRoom.getId(),
                        relationship.getUserA().getId(),
                        MessageType.TEXT,
                        activeQuest.getOpenedAt(),
                        triggerMessageId
                );

        long userBMessageCount =
                chatMessageRepository
                        .countUserMessagesAfterQuestOpenedUntilMessage(
                        chatRoom.getId(),
                        relationship.getUserB().getId(),
                        MessageType.TEXT,
                        activeQuest.getOpenedAt(),
                        triggerMessageId
                );

        if (log.isDebugEnabled()) {
            log.debug(
                    "[AI QUEST] gating - questId={}, title={}, triggerMessageId={}, total={}, userA={}, userB={}",
                    activeQuest.getId(),
                    activeQuest.getQuest().getTitle(),
                    triggerMessageId,
                    totalMessageCount,
                    userAMessageCount,
                    userBMessageCount
            );
        }

        questProgressService.updateQuestProgress(
                activeQuest.getId(),
                (int) totalMessageCount
        );

        boolean enoughMessages =
                totalMessageCount >= MIN_TOTAL_MESSAGES;
        boolean bothUsersParticipated =
                userAMessageCount >= MIN_MESSAGES_PER_USER
                        && userBMessageCount >= MIN_MESSAGES_PER_USER;

        if (!enoughMessages || !bothUsersParticipated) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "[AI QUEST] 평가 조건 미충족 - questId={}, enoughMessages={}, bothUsersParticipated={}",
                        activeQuest.getId(),
                        enoughMessages,
                        bothUsersParticipated
                );
            }
            return Optional.empty();
        }

        if (!shouldEvaluateNow(totalMessageCount)) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "[AI QUEST] 재평가 시점 아님 - questId={}, totalMessageCount={}",
                        activeQuest.getId(),
                        totalMessageCount
                );
            }
            return Optional.empty();
        }

        List<ChatMessage> messages =
                chatMessageRepository
                        .findMessagesAfterQuestOpenedUntilMessage(
                                chatRoom.getId(),
                                MessageType.TEXT,
                                activeQuest.getOpenedAt(),
                                triggerMessageId
                        );

        log.info(
                "[AI QUEST] AI 평가 시작 - questId={}, messageCount={}",
                activeQuest.getId(),
                messages.size()
        );

        AiQuestEvaluationResult evaluation =
                aiQuestEvaluator.evaluate(
                        activeQuest,
                        relationship,
                        messages
                );

        AiQuestEvaluationState state =
                questProgressService.updateAiEvaluation(
                        activeQuest.getId(),
                        evaluation.userAParticipated,
                        evaluation.userBParticipated,
                        evaluation.reciprocalConversation
                );

        boolean completed = state.completed();

        log.info(
                "[AI QUEST] AI 평가 결과 - questId={}, current=({}, {}, {}), accumulated=({}, {}, {}), completed={}",
                activeQuest.getId(),
                evaluation.userAParticipated,
                evaluation.userBParticipated,
                evaluation.reciprocalConversation,
                state.userAParticipated(),
                state.userBParticipated(),
                state.reciprocalConversation(),
                completed
        );

        if (log.isDebugEnabled()) {
            log.debug(
                    "[AI QUEST] AI 평가 사유 - questId={}, reason={}",
                    activeQuest.getId(),
                    evaluation.reason
            );
        }

        if (!completed) {
            return Optional.empty();
        }

        QuestCompletionResult result =
                questProgressService.completeQuest(
                        activeQuest.getId()
                );

        return Optional.of(result);
    }

    private boolean shouldEvaluateNow(long totalMessageCount) {
        if (totalMessageCount < MIN_TOTAL_MESSAGES) {
            return false;
        }

        if (totalMessageCount == MIN_TOTAL_MESSAGES) {
            return true;
        }

        return (totalMessageCount - MIN_TOTAL_MESSAGES)
                % RECHECK_INTERVAL == 0;
    }
}
