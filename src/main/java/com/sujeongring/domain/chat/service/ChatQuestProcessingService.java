package com.sujeongring.domain.chat.service;

import com.sujeongring.domain.chat.dto.response.ChatEventResponse;
import com.sujeongring.domain.chat.dto.response.ChatMessageResponse;
import com.sujeongring.domain.quest.service.QuestCompletionResult;
import com.sujeongring.domain.quest.service.QuestMessageEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatQuestProcessingService {

    private final QuestMessageEvaluationService questMessageEvaluationService;
    private final ChatQuestCompletionService chatQuestCompletionService;
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    public void processTextMessageAsync(
            Long chatRoomId,
            Long triggerMessageId
    ) {
        try {
            Optional<ChatQuestCompletionResult> questResult =
                    processTextMessage(
                            chatRoomId,
                            triggerMessageId
                    );

            if (questResult.isEmpty()) {
                return;
            }

            publishQuestCompletion(
                    chatRoomId,
                    questResult.get()
            );
        } catch (Exception exception) {
            log.warn(
                    "[QUEST] async quest processing failed - chatRoomId={}",
                    chatRoomId,
                    exception
            );
        }
    }

    @Async
    public void processQuestCompletionAsync(
            Long chatRoomId,
            QuestCompletionResult questResult
    ) {
        try {
            ChatQuestCompletionResult completionResult =
                    chatQuestCompletionService.complete(
                            chatRoomId,
                            questResult
                    );

            publishQuestCompletion(
                    chatRoomId,
                    completionResult
            );
        } catch (Exception exception) {
            log.warn(
                    "[QUEST] async quiz completion processing failed - chatRoomId={}",
                    chatRoomId,
                    exception
            );
        }
    }

    public Optional<ChatQuestCompletionResult> processTextMessage(
            Long chatRoomId,
            Long triggerMessageId
    ) {
        Optional<QuestCompletionResult> questResult =
                    questMessageEvaluationService.handleTextMessage(
                            chatRoomId,
                            triggerMessageId
                    );

        if (questResult.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                chatQuestCompletionService.complete(
                        chatRoomId,
                        questResult.get()
                )
        );
    }

    private void publishQuestCompletion(
            Long chatRoomId,
            ChatQuestCompletionResult completionResult
    ) {
        for (ChatMessageResponse systemMessage
                : completionResult.systemMessages()) {
            messagingTemplate.convertAndSend(
                    "/sub/chat/" + chatRoomId,
                    systemMessage
            );
        }

        for (ChatEventResponse event
                : completionResult.events()) {
            messagingTemplate.convertAndSend(
                    "/sub/chat/" + chatRoomId + "/events",
                    event
            );
        }
    }
}
