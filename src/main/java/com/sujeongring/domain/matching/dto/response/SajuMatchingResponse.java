
package com.sujeongring.domain.matching.dto.response;

import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.enums.MatchingType;
import com.sujeongring.domain.user.entity.User;

//사주매칭 실행 결과를 반환
public record SajuMatchingResponse(
        Long recommendationId,
        MatchingType matchingType,
        Integer compatibilityScore,
        CandidateResponse candidate
) {

    public static SajuMatchingResponse from(
            MatchRecommendation recommendation
    ) {
        User candidate = recommendation.getCandidate();

        return new SajuMatchingResponse(
                recommendation.getId(),
                recommendation.getMatchingType(),
                recommendation.getCompatibilityScore(),
                new CandidateResponse(
                        candidate.getId(),
                        candidate.getNickname(),
                        extractAdmissionYear(candidate.getStudentNumber()),
                        candidate.getProfileImageUrl()
                )
        );
    }

    private static Integer extractAdmissionYear(String studentNumber) {
        if (studentNumber == null ||
                !studentNumber.matches("\\d{2,4}")) {
            throw new IllegalArgumentException(
                    "학번 형식이 올바르지 않습니다."
            );
        }

        return Integer.parseInt(
                studentNumber.substring(0, 2)
        );
    }

    public record CandidateResponse(
            Long userId,
            String nickname,
            Integer admissionYear,
            String profileImageUrl
    ) {}
}
