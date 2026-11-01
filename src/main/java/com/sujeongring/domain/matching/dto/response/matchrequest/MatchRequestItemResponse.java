package com.sujeongring.domain.matching.dto.response.matchrequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sujeongring.domain.matching.enums.MatchRequestStatus;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MatchRequestItemResponse(
        Long requestId,
        MatchRequestUserResponse user,
        MatchRequestStatus status,
        LocalDateTime expiresAt,
        Long chatRoomId
) {
}
