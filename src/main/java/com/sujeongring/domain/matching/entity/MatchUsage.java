package com.sujeongring.domain.matching.entity;

import com.sujeongring.domain.matching.enums.MatchUsageType;
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
@Table(name = "match_usages")
public class MatchUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_usage_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "match_recommendation_id",
            nullable = false,
            unique = true
    )
    private MatchRecommendation matchRecommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false, length = 10)
    private MatchUsageType usageType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MatchUsage(
            User user,
            MatchRecommendation matchRecommendation,
            MatchUsageType usageType
    ) {
        this.user = user;
        this.matchRecommendation = matchRecommendation;
        this.usageType = usageType;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
