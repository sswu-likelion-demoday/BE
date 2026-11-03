
package com.sujeongring.domain.matching.dto.response;

public record MatchingHomeResponse(
        RelationshipSlots relationshipSlots
) {
    public record RelationshipSlots(
            int used,
            int available,
            int total
    ) {}
}