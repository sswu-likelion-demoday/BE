package com.sujeongring.domain.matching.repository;

import com.sujeongring.domain.matching.entity.MatchRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRecommendationRepository
        extends JpaRepository<MatchRecommendation, Long> {

    List<MatchRecommendation> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
