package com.sujeongring.domain.relationship.policy;

import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import org.springframework.stereotype.Component;

@Component
public class RelationshipUnlockPolicy {

    // 1단계 완료 -> 관심사
    public boolean isInterestsUnlocked(
            Relationship relationship
    ) {
        return isCompleted(relationship)
                || relationship.getCurrentStage() >= 2;
    }

    // 2단계 완료 -> 취미
    public boolean isHobbiesUnlocked(
            Relationship relationship
    ) {
        return isCompleted(relationship)
                || relationship.getCurrentStage() >= 3;
    }

    // 2단계 완료 -> MBTI
    public boolean isMbtiUnlocked(
            Relationship relationship
    ) {
        return isCompleted(relationship)
                || relationship.getCurrentStage() >= 3;
    }

    // 3단계 완료 -> 이름 · 학번 · 학과
    public boolean isBasicProfileUnlocked(
            Relationship relationship
    ) {
        return isCompleted(relationship)
                || relationship.getCurrentStage() >= 4;
    }

    // 3단계 완료 -> 프로필 사진
    public boolean isProfileImageUnlocked(
            Relationship relationship
    ) {
        return isCompleted(relationship)
                || relationship.getCurrentStage() >= 4;
    }

    // 4단계 완료 -> 외부 연락 수단
    public boolean isContactUnlocked(
            Relationship relationship
    ) {
        return isCompleted(relationship);
    }

    private boolean isCompleted(
            Relationship relationship
    ) {
        return relationship.getStatus()
                == RelationshipStatus.COMPLETED;
    }
}
