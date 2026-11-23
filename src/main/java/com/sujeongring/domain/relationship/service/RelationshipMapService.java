package com.sujeongring.domain.relationship.service;

import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.chat.repository.ChatRoomRepository;
import com.sujeongring.domain.relationship.dto.response.RelationshipMapResponse;
import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.relationship.policy.RelationshipUnlockPolicy;
import com.sujeongring.domain.chat.exception.ChatErrorCode;
import com.sujeongring.domain.chat.exception.ChatException;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.global.error.CommonErrorCode;
import com.sujeongring.global.error.exception.BaseException;
import com.sujeongring.global.s3.ProfileImageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelationshipMapService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final RelationshipUnlockPolicy relationshipUnlockPolicy;
    private final ProfileImageResolver profileImageResolver;

    public RelationshipMapResponse getRelationshipMap(Long userId) {

        User me = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BaseException(CommonErrorCode.NOT_FOUND)
                );

        LocalDateTime oneMonthAgo =
                LocalDateTime.now().minusMonths(1);

        List<RelationshipMapResponse.RelationshipItem> relationships =
                chatRoomRepository.findRelationshipMapRooms(
                                userId,
                                List.of(
                                        RelationshipStatus.ACTIVE,
                                        RelationshipStatus.COMPLETED
                                ),
                                oneMonthAgo
                        )
                        .stream()
                        .map(chatRoom ->
                                toRelationshipItem(
                                        chatRoom,
                                        userId
                                )
                        )
                        .toList();

        return new RelationshipMapResponse(
                new RelationshipMapResponse.Me(
                        me.getId(),
                        me.getNickname()
                ),
                relationships
        );
    }

    private RelationshipMapResponse.RelationshipItem toRelationshipItem(
            ChatRoom chatRoom,
            Long userId
    ) {

        Relationship relationship =
                chatRoom.getRelationship();

        User partner =
                getPartner(
                        relationship,
                        userId
                );

        boolean profileImageUnlocked =
                relationshipUnlockPolicy
                        .isProfileImageUnlocked(relationship);

        int currentStage =
                relationship.getCurrentStage();

        return new RelationshipMapResponse.RelationshipItem(
                relationship.getId(),
                chatRoom.getId(),
                new RelationshipMapResponse.Partner(
                        partner.getId(),
                        partner.getNickname(),
                        profileImageResolver.resolve(
                                partner.getProfileImageUrl(),
                                profileImageUnlocked
                        ),
                        profileImageUnlocked
                ),
                relationship.getStatus(),
                new RelationshipMapResponse.Stage(
                        currentStage,
                        getStageName(currentStage)
                )
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
}