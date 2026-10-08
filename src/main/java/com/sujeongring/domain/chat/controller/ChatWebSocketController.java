package com.sujeongring.domain.chat.controller;

import com.sujeongring.domain.chat.dto.request.ChatMessageSendRequest;
import com.sujeongring.domain.chat.dto.response.ChatMessageResponse;
import com.sujeongring.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{chatRoomId}/messages")
    public void sendMessage(
            @DestinationVariable Long chatRoomId,
            ChatMessageSendRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        ChatMessageResponse response =
                chatMessageService.sendTextMessage(
                        chatRoomId,
                        userId,
                        request.content()
                );

        messagingTemplate.convertAndSend(
                "/sub/chat/" + chatRoomId,
                response
        );
    }
}
