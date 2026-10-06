package com.sujeongring.domain.quest.entity;

import com.sujeongring.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "meeting_verifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_meeting_verifications_quest_user",
                        columnNames = {
                                "relationship_quest_id",
                                "user_id"
                        }
                )
        }
)
public class MeetingVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_verification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_quest_id", nullable = false)
    private RelationshipQuest relationshipQuest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MeetingVerification(
            RelationshipQuest relationshipQuest,
            User user
    ) {
        this.relationshipQuest = relationshipQuest;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.verifiedAt = now;
        this.createdAt = now;
    }
}
