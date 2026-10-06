package com.sujeongring.domain.chat.repository;

import com.sujeongring.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByRelationshipId(Long relationshipId);
}
