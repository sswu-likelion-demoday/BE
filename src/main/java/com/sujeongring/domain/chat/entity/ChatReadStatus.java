package com.sujeongring.domain.chat.entity;

import com.sujeongring.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat_read_status",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_read_status_room_user",
                        columnNames = {"chat_room_id", "user_id"}
                )
        }
)
public class ChatReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_read_status_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    private ChatMessage lastReadMessage;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ChatReadStatus(
            ChatRoom chatRoom,
            User user
    ) {
        this.chatRoom = chatRoom;
        this.user = user;
    }

    public void updateLastReadMessage(ChatMessage message) {
        this.lastReadMessage = message;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }
}
