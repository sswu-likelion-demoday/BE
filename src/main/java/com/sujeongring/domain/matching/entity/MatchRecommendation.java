package com.sujeongring.domain.matching.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_type", nullable = false, length = 10)
    private MatchingType matchingType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MatchRecommendation(
            User user,
            MatchingType matchingType
    ) {
        this.user = user;
        this.matchingType = matchingType;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
