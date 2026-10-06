package com.sujeongring.domain.quest.entity;

import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import com.sujeongring.domain.relationship.entity.Relationship;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "relationship_quests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_relationship_quests_relationship_quest",
                        columnNames = {"relationship_id", "quest_id"}
                )
        }
)
public class RelationshipQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_quest_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_id", nullable = false)
    private Relationship relationship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RelationshipQuestStatus status;

    @Column(nullable = false)
    private int progress;

    @Column(name = "target_progress")
    private Integer targetProgress;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public RelationshipQuest(
            Relationship relationship,
            Quest quest,
            RelationshipQuestStatus status
    ) {
        this.relationship = relationship;
        this.quest = quest;
        this.status = status;
        this.progress = 0;
        this.targetProgress = quest.getTargetValue();

        if (status == RelationshipQuestStatus.ACTIVE) {
            this.openedAt = LocalDateTime.now();
        }
    }

    public void activate() {
        this.status = RelationshipQuestStatus.ACTIVE;
        this.openedAt = LocalDateTime.now();
    }

    public void updateProgress(int progress) {
        this.progress = progress;
    }

    public void complete() {
        this.status = RelationshipQuestStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
