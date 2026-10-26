package com.sujeongring.domain.matching.dto.response;

import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.enums.MatchingType;
import com.sujeongring.domain.user.entity.User;

public record NormalMatchingResponse(
        Long recommendationId,
        MatchingType matchingType,
        Integer matchingScore,
        CandidateResponse candidate
) {
    public record CandidateResponse(
            Long userId,
            String nickname,
            Integer admissionYear,
            String profileImageUrl,
            String introduction
    ) {}

    public static NormalMatchingResponse from(
            MatchRecommendation recommendation
    ) {
        User candidate = recommendation.getCandidate();

        return new NormalMatchingResponse(
                recommendation.getId(),
                recommendation.getMatchingType(),
                recommendation.getMatchingScore(),
                new CandidateResponse(
                        candidate.getId(),
                        candidate.getNickname(),
                        null,
                        candidate.getProfileImageUrl(),
                        null
                )
        );
    }
}