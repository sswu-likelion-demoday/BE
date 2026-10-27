package com.sujeongring.domain.chat.dto.response;

import com.sujeongring.domain.relationship.enums.RelationshipStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ChatRoomListResponse(
        int totalCount,
        int activeCount,
        int completedCount,
        List<ChatRoomSummary> chatRooms
) {

    public record ChatRoomSummary(
            Long chatRoomId,
            Long relationshipId,
            Partner partner,
            RelationshipStatus relationshipStatus,
            Stage currentStage,
            String lastMessage,
            LocalDateTime lastMessageAt,
            long unreadCount
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
