package com.sujeongring.domain.matching.dto.response.matchrequest;

public record AcceptMatchRequestResponse(
        Long requestId,
        Long relationshipId,
        Long chatRoomId
) {
}