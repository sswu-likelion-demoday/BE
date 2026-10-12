package com.sujeongring.domain.chat.controller;

import com.sujeongring.domain.chat.dto.request.ChatMessageSendRequest;
import com.sujeongring.domain.chat.dto.response.ChatEventResponse;
import com.sujeongring.domain.chat.dto.response.ChatMessageResponse;
import com.sujeongring.domain.chat.service.ChatMessageWriteService;
import com.sujeongring.domain.chat.service.ChatQuestProcessingService;
import com.sujeongring.domain.chat.service.ChatQuestCompletionResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageWriteService chatMessageWriteService;
    private final ChatQuestProcessingService chatQuestProcessingService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{chatRoomId}/messages")
    public void sendMessage(
            @DestinationVariable Long chatRoomId,
            @Valid @Payload ChatMessageSendRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        ChatMessageResponse textMessage =
                chatMessageWriteService.saveTextMessage(
                        chatRoomId,
                        userId,
                        request.content()
                );

        // 1. 사용자가 보낸 TEXT
        messagingTemplate.convertAndSend(
                "/sub/chat/" + chatRoomId,
                textMessage
        );

        ChatQuestCompletionResult questResult =
                chatQuestProcessingService.processTextMessage(
                        chatRoomId
                ).orElse(null);

        if (questResult == null) {
            return;
        }

        // 2. 퀘스트 완료 SYSTEM 메시지
        for (ChatMessageResponse systemMessage
                : questResult.systemMessages()) {

            messagingTemplate.convertAndSend(
                    "/sub/chat/" + chatRoomId,
                    systemMessage
            );
        }

        // 3. 프론트 UI 이벤트
        for (ChatEventResponse event
                : questResult.events()) {

            messagingTemplate.convertAndSend(
                    "/sub/chat/" + chatRoomId + "/events",
                    event
            );
        }
    }
}
