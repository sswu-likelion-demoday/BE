package com.sujeongring.domain.chat.service;

import com.sujeongring.domain.chat.dto.response.ChatEventResponse;
import com.sujeongring.domain.chat.dto.response.ChatMessageResponse;
import com.sujeongring.domain.chat.entity.ChatMessage;
import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.chat.enums.MessageType;
import com.sujeongring.domain.chat.exception.ChatErrorCode;
import com.sujeongring.domain.chat.exception.ChatException;
import com.sujeongring.domain.chat.repository.ChatMessageRepository;
import com.sujeongring.domain.chat.repository.ChatRoomRepository;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.exception.QuestErrorCode;
import com.sujeongring.domain.quest.exception.QuestException;
import com.sujeongring.domain.quest.repository.RelationshipQuestRepository;
import com.sujeongring.domain.quest.service.QuestCompletionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatQuestCompletionService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RelationshipQuestRepository relationshipQuestRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChatQuestCompletionResult complete(
            Long chatRoomId,
            QuestCompletionResult result
    ) {
        ChatRoom chatRoom =
                chatRoomRepository
                        .findDetailById(chatRoomId)
                        .orElseThrow(() ->
                                new ChatException(
                                        ChatErrorCode.CHAT_ROOM_NOT_FOUND
                                )
                        );

        List<ChatMessageResponse> systemMessages =
                new ArrayList<>();
        List<ChatEventResponse> events =
                new ArrayList<>();

        RelationshipQuest completedQuest =
                relationshipQuestRepository
                        .findById(result.completedQuestId())
                        .orElseThrow(() ->
                                new QuestException(
                                        QuestErrorCode.QUEST_NOT_FOUND
                                )
                        );

        String systemContent =
                "✦ 퀘스트 완료 · "
                        + completedQuest.getQuest().getTitle();

        ChatMessage systemMessage =
                new ChatMessage(
                        chatRoom,
                        null,
                        MessageType.SYSTEM,
                        systemContent,
                        null
                );

        ChatMessage savedSystemMessage =
                chatMessageRepository.save(
                        systemMessage
                );

        systemMessages.add(
                new ChatMessageResponse(
                        savedSystemMessage.getId(),
                        chatRoom.getId(),
                        null,
                        savedSystemMessage.getMessageType(),
                        savedSystemMessage.getContent(),
                        savedSystemMessage.getCreatedAt()
                )
        );

        if (result.relationshipCompleted()) {
            events.add(
                    ChatEventResponse.informationUnlocked(
                            result.relationshipId(),
                            result.completedStage(),
                            "외부 연락 수단"
                    )
            );

            events.add(
                    ChatEventResponse.relationshipCompleted(
                            result.relationshipId()
                    )
            );

            return new ChatQuestCompletionResult(
                    systemMessages,
                    events
            );
        }

        if (result.stageCompleted()
                && result.newStage() != null) {
            events.add(
                    ChatEventResponse.informationUnlocked(
                            result.relationshipId(),
                            result.newStage(),
                            getUnlockLabel(
                                    result.newStage()
                            )
                    )
            );
        }

        if (result.openedQuestId() != null) {
            RelationshipQuest openedQuest =
                    relationshipQuestRepository
                            .findById(result.openedQuestId())
                            .orElseThrow(() ->
                                    new QuestException(
                                            QuestErrorCode.QUEST_NOT_FOUND
                                    )
                            );

            events.add(
                    ChatEventResponse.questOpened(
                            result.relationshipId(),
                            openedQuest.getId(),
                            openedQuest.getQuest().getStage(),
                            openedQuest.getQuest().getTitle(),
                            openedQuest.getQuest().getDescription()
                    )
            );
        }

        return new ChatQuestCompletionResult(
                systemMessages,
                events
        );
    }

    private String getUnlockLabel(int stage) {
        return switch (stage) {
            case 2 -> "관심사";
            case 3 -> "취미 · MBTI";
            case 4 -> "이름 · 학번 · 학과 · 프로필 사진";
            default -> "";
        };
    }
}
