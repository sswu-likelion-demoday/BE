package com.sujeongring.domain.category.entity;

import com.sujeongring.domain.category.enums.CategoryMatchStatus;
import com.sujeongring.domain.category.enums.CategoryType;
import com.sujeongring.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "category_match_requests")
public class CategoryMatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_match_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType category;

    @Column(name = "member_count", nullable = false)
    private int memberCount;

    @Column(length = 50)
    private String period;

    @Column(columnDefinition = "JSON")
    private String conditions;

    @Column(name = "allow_partial_match", nullable = false)
    private boolean allowPartialMatch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryMatchStatus status;

    @Column(name = "result_viewed_at")
    private LocalDateTime resultViewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CategoryMatchRequest(
            User user,
            CategoryType category,
            int memberCount,
            String period,
            String conditions,
            boolean allowPartialMatch
    ) {
        this.user = user;
        this.category = category;
        this.memberCount = memberCount;
        this.period = period;
        this.conditions = conditions;
        this.allowPartialMatch = allowPartialMatch;
        this.status = CategoryMatchStatus.WAITING;
    }

    public void match() {
        this.status = CategoryMatchStatus.MATCHED;
    }

    public void confirmResult() {
        this.resultViewedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
