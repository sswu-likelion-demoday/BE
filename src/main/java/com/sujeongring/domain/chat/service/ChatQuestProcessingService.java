package com.sujeongring.domain.chat.service;

import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.chat.exception.ChatErrorCode;
import com.sujeongring.domain.chat.exception.ChatException;
import com.sujeongring.domain.chat.repository.ChatRoomRepository;
import com.sujeongring.domain.quest.service.QuestCompletionResult;
import com.sujeongring.domain.quest.service.QuestMessageEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatQuestProcessingService {

    private final ChatRoomRepository chatRoomRepository;
    private final QuestMessageEvaluationService questMessageEvaluationService;
    private final ChatQuestCompletionService chatQuestCompletionService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<ChatQuestCompletionResult> processTextMessage(
            Long chatRoomId
    ) {
        ChatRoom chatRoom =
                chatRoomRepository
                        .findDetailById(chatRoomId)
                        .orElseThrow(() ->
                                new ChatException(
                                        ChatErrorCode.CHAT_ROOM_NOT_FOUND
                                )
                        );

        Optional<QuestCompletionResult> questResult =
                questMessageEvaluationService.handleTextMessage(
                        chatRoomId
                );

        if (questResult.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                chatQuestCompletionService.complete(
                        chatRoom,
                        questResult.get()
                )
        );
    }
}
