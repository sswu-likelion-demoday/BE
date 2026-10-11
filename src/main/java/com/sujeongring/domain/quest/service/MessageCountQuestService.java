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
public class MessageCountQuestService {

    private static final int MIN_MESSAGE_COUNT_PER_USER = 2;

    private final ChatMessageRepository chatMessageRepository;
    private final QuestProgressService questProgressService;

    @Transactional
    public Optional<QuestCompletionResult> handleTextMessage(
            ChatRoom chatRoom,
            RelationshipQuest activeQuest
    ) {
        Relationship relationship =
                chatRoom.getRelationship();

        // MESSAGE_COUNT 퀘스트가 아님
        if (activeQuest.getQuest().getCompletionType()
                != QuestCompletionType.MESSAGE_COUNT) {
            return Optional.empty();
        }

        Integer targetProgress =
                activeQuest.getTargetProgress();

        // 잘못된 데이터 방어
        if (targetProgress == null
                || activeQuest.getOpenedAt() == null) {
            return Optional.empty();
        }

        // 현재 퀘스트가 열린 이후 TEXT 메시지만 계산
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

        // 화면에 보여줄 progress는 목표치까지만 증가
        int progress =
                (int) Math.min(
                        totalMessageCount,
                        targetProgress
                );

        activeQuest.updateProgress(progress);

        boolean targetReached =
                totalMessageCount >= targetProgress;

        boolean bothUsersParticipated =
                userAMessageCount >= MIN_MESSAGE_COUNT_PER_USER
                        && userBMessageCount >= MIN_MESSAGE_COUNT_PER_USER;

        // 아직 완료 조건 미충족
        if (!targetReached || !bothUsersParticipated) {
            return Optional.empty();
        }

        // 모든 완료 방식은 기존 공통 완료 로직으로 연결
        QuestCompletionResult result =
                questProgressService.completeQuest(
                        activeQuest.getId()
                );

        return Optional.of(result);
    }
}
