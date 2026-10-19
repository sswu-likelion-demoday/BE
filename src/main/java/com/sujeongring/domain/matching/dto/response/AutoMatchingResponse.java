
package com.sujeongring.domain.matching.dto.response;

import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.enums.MatchingType;

public record AutoMatchingResponse(
        Long matchRecommendationId,
        MatchingType matchingType,
        Long candidateId,
        String nickname,
        String departmentName,
        String profileImageUrl,
        String mbti
) {
    public static AutoMatchingResponse from(
            MatchRecommendation recommendation
    ) {
        var candidate = recommendation.getCandidate();

        return new AutoMatchingResponse(
                recommendation.getId(),
                recommendation.getMatchingType(),
                candidate.getId(),
                candidate.getNickname(),
                candidate.getDepartmentName(),
                candidate.getProfileImageUrl(),
                candidate.getMbti() == null
                        ? null
                        : candidate.getMbti().name()
        );
    }
}
