package com.sujeongring.domain.quest.entity;

import com.sujeongring.domain.quest.enums.QuestCompletionType;
import com.sujeongring.domain.quest.enums.QuestType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "quests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_quests_stage_order",
                        columnNames = {"stage", "quest_order"}
                )
        }
)
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quest_id")
    private Long id;

    @Column(nullable = false)
    private int stage;

    @Column(name = "quest_order", nullable = false)
    private int questOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "quest_type", nullable = false, length = 20)
    private QuestType questType;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_type", nullable = false, length = 30)
    private QuestCompletionType completionType;

    @Column(name = "target_value")
    private Integer targetValue;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Quest(
            int stage,
            int questOrder,
            QuestType questType,
            String title,
            String description,
            QuestCompletionType completionType,
            Integer targetValue
    ) {
        this.stage = stage;
        this.questOrder = questOrder;
        this.questType = questType;
        this.title = title;
        this.description = description;
        this.completionType = completionType;
        this.targetValue = targetValue;
        this.active = true;
    }
}