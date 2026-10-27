package com.sujeongring.domain.matching.entity;

import com.sujeongring.domain.matching.enums.MatchingType;
import com.sujeongring.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "match_recommendations")
public class MatchRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_recommendation_id")
    private Long id;

    // 매칭을 실행한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 추천된 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_type", nullable = false, length = 10)
    private MatchingType matchingType;

    // 일반 매칭 점수
    @Column(name = "matching_score")
    private Integer matchingScore;

    // 사주 궁합 점수
    @Column(name = "compatibility_score")
    private Integer compatibilityScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MatchRecommendation(
            User user,
            User candidate,
            MatchingType matchingType,
            Integer matchingScore,
            Integer compatibilityScore
    ) {
        this.user = user;
        this.candidate = candidate;
        this.matchingType = matchingType;
        this.matchingScore = matchingScore;
        this.compatibilityScore = compatibilityScore;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
