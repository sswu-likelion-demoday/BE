package com.sujeongring.domain.chat.service;

import com.sujeongring.domain.chat.dto.response.ChatRoomDetailResponse;
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
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import com.sujeongring.domain.quest.repository.RelationshipQuestRepository;
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
    private final RelationshipQuestRepository relationshipQuestRepository;

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

    public ChatRoomDetailResponse getChatRoom(
            Long chatRoomId,
            Long userId
    ) {
        ChatRoom chatRoom = getAccessibleChatRoom(chatRoomId, userId);

        Relationship relationship = chatRoom.getRelationship();

        User partner = getPartner(relationship, userId);

        boolean profileImageUnlocked =
                isProfileImageUnlocked(relationship);

        ChatRoomDetailResponse.Partner partnerResponse =
                new ChatRoomDetailResponse.Partner(
                        partner.getId(),
                        partner.getNickname(),
                        profileImageUnlocked
                                ? partner.getProfileImageUrl()
                                : null,
                        profileImageUnlocked
                );

        // 완료된 관계는 채팅 기록만 읽을 수 있음
        if (relationship.getStatus() == RelationshipStatus.COMPLETED) {
            return new ChatRoomDetailResponse(
                    chatRoom.getId(),
                    relationship.getId(),
                    partnerResponse,
                    relationship.getStatus(),
                    false,
                    null,
                    null,
                    null,
                    null
            );
        }

        int currentStage = relationship.getCurrentStage();

        List<RelationshipQuest> currentStageQuests =
                relationshipQuestRepository
                        .findByRelationshipIdAndStage(
                                relationship.getId(),
                                currentStage
                        );

        int completedCount = (int) currentStageQuests.stream()
                .filter(quest ->
                        quest.getStatus()
                                == RelationshipQuestStatus.COMPLETED)
                .count();

        RelationshipQuest activeQuest =
                currentStageQuests.stream()
                        .filter(quest ->
                                quest.getStatus()
                                        == RelationshipQuestStatus.ACTIVE)
                        .findFirst()
                        .orElse(null);

        ChatRoomDetailResponse.CurrentQuest currentQuest =
                activeQuest == null
                        ? null
                        : new ChatRoomDetailResponse.CurrentQuest(
                        activeQuest.getId(),
                        activeQuest.getQuest().getTitle(),
                        activeQuest.getQuest().getQuestType()
                );

        return new ChatRoomDetailResponse(
                chatRoom.getId(),
                relationship.getId(),
                partnerResponse,
                relationship.getStatus(),
                true,
                new ChatRoomDetailResponse.Stage(
                        currentStage,
                        getStageName(currentStage)
                ),
                new ChatRoomDetailResponse.QuestProgress(
                        completedCount,
                        currentStageQuests.size()
                ),
                currentQuest,
                getUnlockGuideMessage(currentStage)
        );
    }

    @Transactional(readOnly = true)
    public ChatRoom getAccessibleChatRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository
                .findDetailById(chatRoomId)
                .orElseThrow(() -> new ChatException(
                        ChatErrorCode.CHAT_ROOM_NOT_FOUND
                ));

        Relationship relationship = chatRoom.getRelationship();

        boolean isParticipant =
                relationship.getUserA().getId().equals(userId)
                        || relationship.getUserB().getId().equals(userId);

        if (!isParticipant) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_ACCESS_DENIED
            );
        }

        if (relationship.getStatus() == RelationshipStatus.ENDED) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_ENDED
            );
        }

        return chatRoom;
    }

    @Transactional
    public void markAsRead(
            Long chatRoomId,
            Long userId,
            Long lastReadMessageId
    ) {
        // 채팅방 존재 + 참여자 + ENDED 여부 검증
        getAccessibleChatRoom(chatRoomId, userId);

        // 실제 이 채팅방에 속한 메시지인지 확인
        ChatMessage lastReadMessage = chatMessageRepository
                .findById(lastReadMessageId)
                .orElseThrow(() ->
                        new ChatException(
                                ChatErrorCode.CHAT_MESSAGE_NOT_FOUND
                        )
                );

        if (!lastReadMessage.getChatRoom().getId().equals(chatRoomId)) {
            throw new ChatException(
                    ChatErrorCode.CHAT_MESSAGE_NOT_FOUND
            );
        }

        ChatReadStatus readStatus = chatReadStatusRepository
                .findByChatRoomIdAndUserId(
                        chatRoomId,
                        userId
                )
                .orElseThrow(() ->
                        new ChatException(
                                ChatErrorCode.CHAT_READ_STATUS_NOT_FOUND
                        )
                );

        ChatMessage currentLastRead =
                readStatus.getLastReadMessage();

        // 읽음 위치가 뒤로 돌아가지 않도록 함
        if (currentLastRead == null
                || lastReadMessage.getId() > currentLastRead.getId()) {

            readStatus.updateLastReadMessage(
                    lastReadMessage
            );
        }
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

    private String getUnlockGuideMessage(int stage) {
        return switch (stage) {
            case 1 -> "1단계를 마치면 상대의 관심사가 열려요";
            case 2 -> "2단계를 마치면 상대의 취미 · MBTI가 열려요";
            case 3 -> "3단계를 마치면 상대의 이름 · 학번 · 학과 · 프로필 사진이 열려요";
            case 4 -> "4단계를 마치면 상대의 외부 연락 수단이 열려요";
            default -> null;
        };
    }
}
