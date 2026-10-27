package com.sujeongring.domain.chat.dto.response;

import java.util.List;

public record ChatSendResult(
        ChatMessageResponse message,
        List<ChatMessageResponse> systemMessages,
        List<ChatEventResponse> events
) {
}
