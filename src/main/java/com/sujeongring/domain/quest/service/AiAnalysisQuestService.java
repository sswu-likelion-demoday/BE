package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.chat.enums.MessageType;
import com.sujeongring.domain.chat.repository.ChatMessageRepository;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.enums.QuestCompletionType;
import com.sujeongring.domain.relationship.entity.Relationship;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiAnalysisQuestService {

    private static final int MIN_TOTAL_MESSAGES = 8;
    private static final int MIN_MESSAGES_PER_USER = 2;
    private static final int RECHECK_INTERVAL = 2;

    private final ChatMessageRepository chatMessageRepository;
    private final QuestProgressService questProgressService;

    @Transactional
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

        activeQuest.updateProgress((int) totalMessageCount);

        boolean enoughMessages =
                totalMessageCount >= MIN_TOTAL_MESSAGES;
        boolean bothUsersParticipated =
                userAMessageCount >= MIN_MESSAGES_PER_USER
                        && userBMessageCount >= MIN_MESSAGES_PER_USER;

        if (!enoughMessages || !bothUsersParticipated) {
            return Optional.empty();
        }

        if (!shouldEvaluateNow(totalMessageCount)) {
            return Optional.empty();
        }

        // 일단 실제 LLM 호출 전 임시 완료 처리
        boolean completed = true;

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
