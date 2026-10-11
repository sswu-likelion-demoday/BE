package com.sujeongring.domain.chat.service;

import com.sujeongring.domain.chat.dto.response.ChatEventResponse;
import com.sujeongring.domain.chat.dto.response.ChatMessageResponse;

import java.util.List;

public record ChatQuestCompletionResult(
        List<ChatMessageResponse> systemMessages,
        List<ChatEventResponse> events
) {
}
