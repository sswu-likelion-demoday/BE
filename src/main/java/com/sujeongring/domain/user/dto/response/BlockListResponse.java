package com.sujeongring.domain.user.dto.response;

import java.util.List;

public record BlockListResponse(
        List<BlockedUserResponse> blockedUsers
) {
}
