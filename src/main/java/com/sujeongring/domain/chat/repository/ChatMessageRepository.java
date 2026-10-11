package com.sujeongring.domain.chat.repository;

import com.sujeongring.domain.chat.entity.ChatMessage;
import com.sujeongring.domain.chat.enums.MessageType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    Optional<ChatMessage> findTopByChatRoomIdOrderByIdDesc(
            Long chatRoomId
    );

    List<ChatMessage> findByChatRoomIdOrderByIdDesc(
            Long chatRoomId,
            Pageable pageable
    );

    List<ChatMessage> findByChatRoomIdAndIdLessThanOrderByIdDesc(
            Long chatRoomId,
            Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(m)
            FROM ChatMessage m
            WHERE m.chatRoom.id = :chatRoomId
              AND m.id > :lastReadMessageId
              AND (m.sender IS NULL OR m.sender.id <> :userId)
            """)
    long countUnreadMessages(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId,
            @Param("lastReadMessageId") Long lastReadMessageId
    );

    @Query("""
        SELECT COUNT(m)
        FROM ChatMessage m
        WHERE m.chatRoom.id = :chatRoomId
          AND m.messageType = :messageType
          AND m.createdAt >= :openedAt
        """)
    long countMessagesAfterQuestOpened(
            @Param("chatRoomId") Long chatRoomId,
            @Param("messageType") MessageType messageType,
            @Param("openedAt") LocalDateTime openedAt
    );

    @Query("""
        SELECT COUNT(m)
        FROM ChatMessage m
        WHERE m.chatRoom.id = :chatRoomId
          AND m.sender.id = :userId
          AND m.messageType = :messageType
          AND m.createdAt >= :openedAt
        """)
    long countUserMessagesAfterQuestOpened(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId,
            @Param("messageType") MessageType messageType,
            @Param("openedAt") LocalDateTime openedAt
    );
}
