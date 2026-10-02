package com.sujeongring.domain.matching.repository;

import com.sujeongring.domain.matching.entity.MatchRecommendationCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRecommendationCandidateRepository
        extends JpaRepository<MatchRecommendationCandidate, Long> {

    List<MatchRecommendationCandidate>
    findAllByMatchRecommendationIdOrderByRecommendationOrderAsc(
            Long matchRecommendationId
    );
}
