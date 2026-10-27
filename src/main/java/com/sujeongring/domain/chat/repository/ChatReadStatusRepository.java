package com.sujeongring.domain.chat.repository;

import com.sujeongring.domain.chat.entity.ChatReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatReadStatusRepository
        extends JpaRepository<ChatReadStatus, Long> {

    Optional<ChatReadStatus> findByChatRoomIdAndUserId(
            Long chatRoomId,
            Long userId
    );
}
