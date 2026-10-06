package com.sujeongring.domain.matching.entity;

import com.sujeongring.domain.matching.enums.MatchRequestStatus;
import com.sujeongring.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "match_requests")
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_recommendation_id", nullable = false)
    private MatchRecommendation matchRecommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchRequestStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    // 보낸 사용자가 수락 결과를 확인한 시점
    @Column(name = "sender_confirmed_at")
    private LocalDateTime senderConfirmedAt;

    public MatchRequest(
            MatchRecommendation matchRecommendation,
            User sender,
            User receiver
    ) {
        this.matchRecommendation = matchRecommendation;
        this.sender = sender;
        this.receiver = receiver;
        this.status = MatchRequestStatus.PENDING;
    }

    public void accept() {
        this.status = MatchRequestStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = MatchRequestStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = MatchRequestStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = MatchRequestStatus.EXPIRED;
    }

    public void confirmBySender() {
        this.senderConfirmedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.expiresAt = now.plusDays(3);
    }
}
