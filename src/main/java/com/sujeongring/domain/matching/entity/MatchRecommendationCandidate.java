package com.sujeongring.domain.matching.entity;

import com.sujeongring.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "match_recommendation_candidates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recommendation_candidate",
                        columnNames = {"match_recommendation_id", "candidate_id"}
                )
        }
)
public class MatchRecommendationCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_recommendation_candidate_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_recommendation_id", nullable = false)
    private MatchRecommendation matchRecommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(name = "recommendation_order", nullable = false)
    private int recommendationOrder;

    @Column(name = "matching_score")
    private Integer matchingScore;

    @Column(name = "compatibility_score")
    private Integer compatibilityScore;

    public MatchRecommendationCandidate(
            MatchRecommendation matchRecommendation,
            User candidate,
            int recommendationOrder,
            Integer matchingScore,
            Integer compatibilityScore
    ) {
        this.matchRecommendation = matchRecommendation;
        this.candidate = candidate;
        this.recommendationOrder = recommendationOrder;
        this.matchingScore = matchingScore;
        this.compatibilityScore = compatibilityScore;
    }
}
