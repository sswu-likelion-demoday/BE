package com.sujeongring.domain.matching.dto.response;

public record RelationshipSlotsResponse(
        long used,
        long available,
        long total
) {
}
