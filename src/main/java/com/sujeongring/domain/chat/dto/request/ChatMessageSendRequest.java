package com.sujeongring.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageSendRequest(

        @NotBlank(message = "메시지 내용은 비어 있을 수 없습니다.")
        String content
) {
}
