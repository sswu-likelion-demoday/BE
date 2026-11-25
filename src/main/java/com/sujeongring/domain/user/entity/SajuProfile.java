
package com.sujeongring.domain.user.entity;

import com.sujeongring.domain.user.enums.CalendarType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "saju_profiles")
public class SajuProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saju_profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_type", length = 10)
    private CalendarType calendarType;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_time")
    private LocalTime birthTime;

    @Column(name = "saju_data", columnDefinition = "TEXT")
    private String sajuData;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "matching_enabled", nullable = false)
    private boolean matchingEnabled = false;

    // 최초 사주 프로필 생성
    public SajuProfile(
            User user,
            boolean matchingEnabled,
            CalendarType calendarType,
            LocalDate birthDate,
            LocalTime birthTime
    ) {
        this.user = user;
        updateSettings(
                matchingEnabled,
                calendarType,
                birthDate,
                birthTime
        );
    }

    // 기존 사주 프로필 설정 변경
    public void updateSettings(
            boolean matchingEnabled,
            CalendarType calendarType,
            LocalDate birthDate,
            LocalTime birthTime
    ) {
        if (matchingEnabled &&
                (calendarType == null || birthDate == null)) {
            throw new IllegalArgumentException(
                    "사주 매칭을 활성화하려면 생년월일과 양력/음력 구분이 필요합니다."
            );
        }

        this.matchingEnabled = matchingEnabled;

        // OFF 요청에서 출생 정보가 없으면 기존 정보 유지
        if (calendarType != null && birthDate != null) {
            boolean birthInfoChanged =
                    this.calendarType != calendarType
                            || !birthDate.equals(this.birthDate)
                            || !java.util.Objects.equals(
                            this.birthTime, birthTime
                    );

            this.calendarType = calendarType;
            this.birthDate = birthDate;
            this.birthTime = birthTime;

            // 출생 정보가 변경되면 기존 분석 결과 무효화
            if (birthInfoChanged) {
                this.sajuData = null;
                this.analyzedAt = null;
            }
        }
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
