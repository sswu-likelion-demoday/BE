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
            RelationshipQuest activeQuest
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
                chatMessageRepository.countMessagesAfterQuestOpened(
                        chatRoom.getId(),
                        MessageType.TEXT,
                        activeQuest.getOpenedAt()
                );

        long userAMessageCount =
                chatMessageRepository.countUserMessagesAfterQuestOpened(
                        chatRoom.getId(),
                        relationship.getUserA().getId(),
                        MessageType.TEXT,
                        activeQuest.getOpenedAt()
                );

        long userBMessageCount =
                chatMessageRepository.countUserMessagesAfterQuestOpened(
                        chatRoom.getId(),
                        relationship.getUserB().getId(),
                        MessageType.TEXT,
                        activeQuest.getOpenedAt()
                );

        log.info(
                "[AI QUEST] questId={}, title={}, total={}, userA={}, userB={}",
                activeQuest.getId(),
                activeQuest.getQuest().getTitle(),
                totalMessageCount,
                userAMessageCount,
                userBMessageCount
        );

        activeQuest.updateProgress((int) totalMessageCount);

        boolean enoughMessages =
                totalMessageCount >= MIN_TOTAL_MESSAGES;
        boolean bothUsersParticipated =
                userAMessageCount >= MIN_MESSAGES_PER_USER
                        && userBMessageCount >= MIN_MESSAGES_PER_USER;

        if (!enoughMessages || !bothUsersParticipated) {
            log.info(
                    "[AI QUEST] 평가 조건 미충족 - enoughMessages={}, bothUsersParticipated={}",
                    enoughMessages,
                    bothUsersParticipated
            );
            return Optional.empty();
        }

        if (!shouldEvaluateNow(totalMessageCount)) {
            log.info(
                    "[AI QUEST] 재평가 시점 아님 - totalMessageCount={}",
                    totalMessageCount
            );
            return Optional.empty();
        }

        List<ChatMessage> messages =
                chatMessageRepository
                        .findMessagesAfterQuestOpened(
                                chatRoom.getId(),
                                MessageType.TEXT,
                                activeQuest.getOpenedAt()
                        );

        log.info(
                "[AI QUEST] AI 평가 시작 - questId={}, title={}, messageCount={}",
                activeQuest.getId(),
                activeQuest.getQuest().getTitle(),
                messages.size()
        );

        AiQuestEvaluationResult evaluation =
                aiQuestEvaluator.evaluate(
                        activeQuest,
                        relationship,
                        messages
                );

        boolean completed =
                evaluation.userAParticipated
                        && evaluation.userBParticipated
                        && evaluation.reciprocalConversation;

        log.info(
                "[AI QUEST] AI 평가 결과 - userAParticipated={}, userBParticipated={}, reciprocalConversation={}, completed={}, reason={}",
                evaluation.userAParticipated,
                evaluation.userBParticipated,
                evaluation.reciprocalConversation,
                completed,
                evaluation.reason
        );

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
