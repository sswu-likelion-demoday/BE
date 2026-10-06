package com.sujeongring.domain.quest.entity;

import com.sujeongring.domain.quest.enums.AiQuizStatus;
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
        name = "ai_quizzes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ai_quizzes_quest_answer_user",
                        columnNames = {
                                "relationship_quest_id",
                                "answer_user_id"
                        }
                )
        }
)
public class AiQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_quiz_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_quest_id", nullable = false)
    private RelationshipQuest relationshipQuest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_user_id", nullable = false)
    private User answerUser;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "JSON")
    private String options;

    @Column(name = "correct_option", nullable = false)
    private int correctOption;

    @Column(name = "selected_option")
    private Integer selectedOption;

    @Column(name = "is_correct")
    private Boolean correct;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiQuizStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    public AiQuiz(
            RelationshipQuest relationshipQuest,
            User targetUser,
            User answerUser,
            String question,
            String options,
            int correctOption
    ) {
        this.relationshipQuest = relationshipQuest;
        this.targetUser = targetUser;
        this.answerUser = answerUser;
        this.question = question;
        this.options = options;
        this.correctOption = correctOption;
        this.status = AiQuizStatus.OPEN;
    }

    public void answer(int selectedOption) {
        this.selectedOption = selectedOption;
        this.correct = this.correctOption == selectedOption;
        this.status = AiQuizStatus.ANSWERED;
        this.answeredAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
