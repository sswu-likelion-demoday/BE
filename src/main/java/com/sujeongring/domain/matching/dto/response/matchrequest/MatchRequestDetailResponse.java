
package com.sujeongring.domain.matching.dto.response.matchrequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sujeongring.domain.matching.enums.MatchRequestStatus;
import com.sujeongring.domain.matching.enums.MatchingType;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MatchRequestDetailResponse(
        Long requestId,
        String direction,
        MatchRequestStatus status,
        MatchingType matchingType,
        Integer compatibilityScore,
        UserProfile user,
        LocalDateTime expiresAt
) {
    public record UserProfile(
            Long userId,
            String nickname,
            Integer admissionYear,
            String profileImageUrl
    ) {
    }
}
