package com.sujeongring.domain.chat.controller;

import com.sujeongring.domain.chat.dto.request.ChatMessageSendRequest;
import com.sujeongring.domain.chat.dto.response.ChatMessageResponse;
import com.sujeongring.domain.chat.service.ChatMessageWriteService;
import com.sujeongring.domain.chat.service.ChatQuestProcessingService;
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

        chatQuestProcessingService.processTextMessageAsync(
                chatRoomId,
                textMessage.messageId()
        );
    }
}
