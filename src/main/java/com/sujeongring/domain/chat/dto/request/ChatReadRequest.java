package com.sujeongring.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChatReadRequest(

        @NotNull
        Long lastReadMessageId

) {
}
