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
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_type", length = 30)
    private SystemMessageType systemType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ChatMessage(
            ChatRoom chatRoom,
            User sender,
            MessageType messageType,
            SystemMessageType systemType,
            String content,
            String imageUrl,
            Long referenceId
    ) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.messageType = messageType;
        this.systemType = systemType;
        this.content = content;
        this.imageUrl = imageUrl;
        this.referenceId = referenceId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
