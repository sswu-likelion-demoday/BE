package com.sujeongring.domain.chat.dto.response;

import com.sujeongring.domain.chat.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;

public record ChatMessageListResponse(
        List<Message> messages,
        Long nextCursor,
        boolean hasNext
) {

    public record Message(
            Long messageId,
            Long senderId,
            MessageType messageType,
            String content,
            LocalDateTime createdAt
    ) {
    }
}
