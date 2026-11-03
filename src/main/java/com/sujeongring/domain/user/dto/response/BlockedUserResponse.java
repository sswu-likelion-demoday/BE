package com.sujeongring.domain.user.dto.response;

import java.time.LocalDateTime;

public record BlockedUserResponse(
        Long userId,
        String nickname,
        LocalDateTime blockedAt
) {
}
