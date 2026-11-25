
package com.sujeongring.domain.matching.dto.response.matchrequest;

import com.sujeongring.domain.matching.dto.response.RelationshipSlotsResponse;

import java.util.List;

public record MatchRequestListResponse(
        RelationshipSlotsResponse relationshipSlots,
        int receivedCount,
        int sentCount,
        List<MatchRequestItemResponse> receivedRequests,
        List<MatchRequestItemResponse> sentRequests
) {
    public static MatchRequestListResponse of(
            RelationshipSlotsResponse slots,
            List<MatchRequestItemResponse> received,
            List<MatchRequestItemResponse> sent
    ) {
        return new MatchRequestListResponse(
                slots,
                received.size(),
                sent.size(),
                received,
                sent
        );
    }
}
