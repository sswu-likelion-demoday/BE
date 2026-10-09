package com.sujeongring.domain.chat.service;

import com.sujeongring.domain.chat.dto.response.ChatMessageListResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatMessageListResponse getMessages(
            Long chatRoomId,
            Long userId,
            Long cursor,
            int size
    ) {
        validateSize(size);

        ChatRoom chatRoom = chatRoomRepository
                .findDetailById(chatRoomId)
                .orElseThrow(() -> new ChatException(
                        ChatErrorCode.CHAT_ROOM_NOT_FOUND
                ));

        validateAccess(chatRoom, userId);

        // hasNext 확인을 위해 요청 개수보다 1개 더 조회
        PageRequest pageable = PageRequest.of(0, size + 1);

        List<ChatMessage> fetchedMessages;

        if (cursor == null) {
            fetchedMessages =
                    chatMessageRepository.findByChatRoomIdOrderByIdDesc(
                            chatRoomId,
                            pageable
                    );
        } else {
            fetchedMessages =
                    chatMessageRepository
                            .findByChatRoomIdAndIdLessThanOrderByIdDesc(
                                    chatRoomId,
                                    cursor,
                                    pageable
                            );
        }

        boolean hasNext = fetchedMessages.size() > size;

        List<ChatMessage> page =
                hasNext
                        ? new ArrayList<>(
                        fetchedMessages.subList(0, size)
                )
                        : new ArrayList<>(fetchedMessages);

        Long nextCursor =
                hasNext && !page.isEmpty()
                        ? page.get(page.size() - 1).getId()
                        : null;

        // DB에서는 최신 -> 과거로 가져왔지만
        // 채팅 화면에는 과거 -> 최신 순으로 반환
        Collections.reverse(page);

        List<ChatMessageListResponse.Message> messages =
                page.stream()
                        .map(this::toResponse)
                        .toList();

        return new ChatMessageListResponse(
                messages,
                nextCursor,
                hasNext
        );
    }

    @Transactional
    public ChatMessageResponse sendTextMessage(
            Long chatRoomId,
            Long userId,
            String content
    ) {
        ChatRoom chatRoom = chatRoomRepository
                .findDetailById(chatRoomId)
                .orElseThrow(() -> new ChatException(
                        ChatErrorCode.CHAT_ROOM_NOT_FOUND
                ));

        Relationship relationship = chatRoom.getRelationship();

        validateParticipant(relationship, userId);
        validateWritable(relationship);

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ChatException(
                        ChatErrorCode.CHAT_ROOM_ACCESS_DENIED
                ));

        ChatMessage message = new ChatMessage(
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

    private ChatMessageListResponse.Message toResponse(
            ChatMessage message
    ) {
        return new ChatMessageListResponse.Message(
                message.getId(),
                message.getSender() != null
                        ? message.getSender().getId()
                        : null,
                message.getMessageType(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private void validateAccess(
            ChatRoom chatRoom,
            Long userId
    ) {
        Relationship relationship = chatRoom.getRelationship();

        boolean participant =
                relationship.getUserA().getId().equals(userId)
                        || relationship.getUserB().getId().equals(userId);

        if (!participant) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_ACCESS_DENIED
            );
        }

        if (relationship.getStatus() == RelationshipStatus.ENDED) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_ENDED
            );
        }
    }

    private void validateSize(int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ChatException(
                    ChatErrorCode.INVALID_MESSAGE_PAGE_SIZE
            );
        }
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
