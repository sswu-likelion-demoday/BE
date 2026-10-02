package com.sujeongring.domain.relationship.entity;

import com.sujeongring.domain.matching.entity.MatchRequest;
import com.sujeongring.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "relationships")
public class Relationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_request_id", nullable = false, unique = true)
    private MatchRequest matchRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RelationshipStatus status;

    @Column(name = "current_stage", nullable = false)
    private int currentStage;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public Relationship(
            User userA,
            User userB,
            MatchRequest matchRequest
    ) {
        this.userA = userA;
        this.userB = userB;
        this.matchRequest = matchRequest;
        this.status = RelationshipStatus.ACTIVE;
        this.currentStage = 1;
    }

    @PrePersist
    protected void onCreate() {
        this.startedAt = LocalDateTime.now();
    }

    public void advanceStage() {
        this.currentStage++;
    }

    public void complete() {
        this.status = RelationshipStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void end() {
        this.status = RelationshipStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }
}
