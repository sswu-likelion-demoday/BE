package com.sujeongring.domain.chat.repository;

import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
            SELECT cr
            FROM ChatRoom cr
            JOIN FETCH cr.relationship r
            JOIN FETCH r.userA
            JOIN FETCH r.userB
            WHERE (r.userA.id = :userId OR r.userB.id = :userId)
              AND r.status IN :statuses
            """)
    List<ChatRoom> findVisibleChatRooms(
            @Param("userId") Long userId,
            @Param("statuses") Collection<RelationshipStatus> statuses
    );

    @Query("""
        SELECT cr
        FROM ChatRoom cr
        JOIN FETCH cr.relationship r
        JOIN FETCH r.userA
        JOIN FETCH r.userB
        WHERE cr.id = :chatRoomId
        """)
    Optional<ChatRoom> findDetailById(
            @Param("chatRoomId") Long chatRoomId
    );
}
