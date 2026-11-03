package com.sujeongring.domain.matching.dto.response.matchrequest;

import com.sujeongring.domain.matching.entity.MatchRequest;
import com.sujeongring.domain.matching.enums.MatchRequestStatus;

import java.time.LocalDateTime;

public record CreateMatchRequestResponse(
        Long requestId,
        MatchRequestStatus status,
        LocalDateTime expiresAt
) {
    public static CreateMatchRequestResponse from(MatchRequest request) {
        return new CreateMatchRequestResponse(
                request.getId(),
                request.getStatus(),
                request.getExpiresAt()
        );
    }
}
