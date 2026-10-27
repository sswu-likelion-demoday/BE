package com.sujeongring.domain.chat.dto.response;

import com.sujeongring.domain.chat.enums.MessageType;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long messageId,
        Long chatRoomId,
        Long senderId,
        MessageType messageType,
        String content,
        LocalDateTime createdAt
) {
}
