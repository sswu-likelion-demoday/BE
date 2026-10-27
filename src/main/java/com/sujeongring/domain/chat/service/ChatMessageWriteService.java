package com.sujeongring.domain.chat.service;

import com.sujeongring.domain.chat.dto.response.ChatMessageResponse;
import com.sujeongring.domain.chat.entity.ChatMessage;
import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.chat.enums.MessageType;
import com.sujeongring.domain.chat.exception.ChatErrorCode;
import com.sujeongring.domain.chat.exception.ChatException;
import com.sujeongring.domain.chat.repository.ChatMessageRepository;
import com.sujeongring.domain.chat.repository.ChatRoomRepository;
import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageWriteService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChatMessageResponse saveTextMessage(
            Long chatRoomId,
            Long userId,
            String content
    ) {
        ChatRoom chatRoom =
                chatRoomRepository
                        .findDetailById(chatRoomId)
                        .orElseThrow(() ->
                                new ChatException(
                                        ChatErrorCode.CHAT_ROOM_NOT_FOUND
                                )
                        );

        Relationship relationship = chatRoom.getRelationship();

        validateParticipant(relationship, userId);
        validateWritable(relationship);

        User sender =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ChatException(
                                        ChatErrorCode.CHAT_ROOM_ACCESS_DENIED
                                )
                        );

        ChatMessage message =
                new ChatMessage(
                        chatRoom,
                        sender,
                        MessageType.TEXT,
                        content,
                        null
                );

        ChatMessage savedMessage =
                chatMessageRepository.save(message);

        return new ChatMessageResponse(
                savedMessage.getId(),
                chatRoom.getId(),
                sender.getId(),
                savedMessage.getMessageType(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt()
        );
    }

    private void validateParticipant(
            Relationship relationship,
            Long userId
    ) {
        boolean participant =
                relationship.getUserA().getId().equals(userId)
                        || relationship.getUserB().getId().equals(userId);

        if (!participant) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_ACCESS_DENIED
            );
        }
    }

    private void validateWritable(Relationship relationship) {
        if (relationship.getStatus() != RelationshipStatus.ACTIVE) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_NOT_WRITABLE
            );
        }
    }
}
