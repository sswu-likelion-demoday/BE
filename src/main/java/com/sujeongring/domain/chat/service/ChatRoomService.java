package com.sujeongring.domain.chat.service;

import com.sujeongring.domain.chat.dto.response.ChatRoomListResponse;
import com.sujeongring.domain.chat.entity.ChatMessage;
import com.sujeongring.domain.chat.entity.ChatReadStatus;
import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.chat.exception.ChatErrorCode;
import com.sujeongring.domain.chat.exception.ChatException;
import com.sujeongring.domain.chat.repository.ChatMessageRepository;
import com.sujeongring.domain.chat.repository.ChatReadStatusRepository;
import com.sujeongring.domain.chat.repository.ChatRoomRepository;
import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;

    public ChatRoomListResponse getChatRooms(Long userId) {

        List<ChatRoomSummaryWithSortTime> summaries =
                chatRoomRepository.findVisibleChatRooms(
                                userId,
                                List.of(
                                        RelationshipStatus.ACTIVE,
                                        RelationshipStatus.COMPLETED
                                )
                        )
                        .stream()
                        .map(chatRoom -> toSummary(chatRoom, userId))
                        .sorted(
                                Comparator.comparing(
                                        ChatRoomSummaryWithSortTime::sortTime
                                ).reversed()
                        )
                        .toList();

        List<ChatRoomListResponse.ChatRoomSummary> chatRooms =
                summaries.stream()
                        .map(ChatRoomSummaryWithSortTime::response)
                        .toList();

        int activeCount = (int) chatRooms.stream()
                .filter(chatRoom ->
                        chatRoom.relationshipStatus()
                                == RelationshipStatus.ACTIVE)
                .count();

        int completedCount = (int) chatRooms.stream()
                .filter(chatRoom ->
                        chatRoom.relationshipStatus()
                                == RelationshipStatus.COMPLETED)
                .count();

        return new ChatRoomListResponse(
                chatRooms.size(),
                activeCount,
                completedCount,
                chatRooms
        );
    }

    private ChatRoomSummaryWithSortTime toSummary(
            ChatRoom chatRoom,
            Long userId
    ) {
        Relationship relationship = chatRoom.getRelationship();

        User partner = getPartner(relationship, userId);

        ChatMessage lastMessage = chatMessageRepository
                .findTopByChatRoomIdOrderByIdDesc(chatRoom.getId())
                .orElse(null);

        ChatReadStatus readStatus = chatReadStatusRepository
                .findByChatRoomIdAndUserId(chatRoom.getId(), userId)
                .orElse(null);

        long lastReadMessageId =
                readStatus != null && readStatus.getLastReadMessage() != null
                        ? readStatus.getLastReadMessage().getId()
                        : 0L;

        long unreadCount = chatMessageRepository.countUnreadMessages(
                chatRoom.getId(),
                userId,
                lastReadMessageId
        );

        boolean profileImageUnlocked =
                isProfileImageUnlocked(relationship);

        ChatRoomListResponse.Stage currentStage =
                getCurrentStage(relationship);

        ChatRoomListResponse.ChatRoomSummary response =
                new ChatRoomListResponse.ChatRoomSummary(
                        chatRoom.getId(),
                        relationship.getId(),
                        new ChatRoomListResponse.Partner(
                                partner.getId(),
                                partner.getNickname(),
                                profileImageUnlocked
                                        ? partner.getProfileImageUrl()
                                        : null,
                                profileImageUnlocked
                        ),
                        relationship.getStatus(),
                        currentStage,
                        lastMessage != null
                                ? lastMessage.getContent()
                                : null,
                        lastMessage != null
                                ? lastMessage.getCreatedAt()
                                : null,
                        unreadCount
                );

        // 아직 메시지가 없는 새 채팅방은 채팅방 생성 시간을 정렬 기준으로 사용
        LocalDateTime sortTime = lastMessage != null
                ? lastMessage.getCreatedAt()
                : chatRoom.getCreatedAt();

        return new ChatRoomSummaryWithSortTime(
                response,
                sortTime
        );
    }

    private User getPartner(
            Relationship relationship,
            Long userId
    ) {
        if (relationship.getUserA().getId().equals(userId)) {
            return relationship.getUserB();
        }

        if (relationship.getUserB().getId().equals(userId)) {
            return relationship.getUserA();
        }

        throw new ChatException(
                ChatErrorCode.CHAT_ROOM_ACCESS_DENIED
        );
    }

    private ChatRoomListResponse.Stage getCurrentStage(
            Relationship relationship
    ) {
        if (relationship.getStatus() == RelationshipStatus.COMPLETED) {
            return null;
        }

        int stage = relationship.getCurrentStage();

        return new ChatRoomListResponse.Stage(
                stage,
                getStageName(stage)
        );
    }

    private boolean isProfileImageUnlocked(
            Relationship relationship
    ) {
        if (relationship.getStatus() == RelationshipStatus.COMPLETED) {
            return true;
        }

        // 3단계 완료 후 프로필 사진 해금
        return relationship.getCurrentStage() >= 4;
    }

    private String getStageName(int stage) {
        return switch (stage) {
            case 1 -> "첫 인사";
            case 2 -> "알아가는 중";
            case 3 -> "가까워지는 중";
            case 4 -> "인연";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 관계 단계입니다: " + stage
            );
        };
    }

    private record ChatRoomSummaryWithSortTime(
            ChatRoomListResponse.ChatRoomSummary response,
            LocalDateTime sortTime
    ) {
    }
}
