package com.sujeongring.domain.relationship.dto.response;

import com.sujeongring.domain.relationship.enums.RelationshipStatus;

import java.util.List;

public record RelationshipMapResponse(
        Me me,
        List<RelationshipItem> relationships
) {

    public record Me(
            Long userId,
            String nickname
    ) {
    }

    public record RelationshipItem(
            Long relationshipId,
            Long chatRoomId,
            Partner partner,
            RelationshipStatus relationshipStatus,
            Stage currentStage
    ) {
    }

    public record Partner(
            Long userId,
            String nickname,
            String profileImageUrl,
            boolean profileImageUnlocked
    ) {
    }

    public record Stage(
            int stage,
            String name
    ) {
    }
}
