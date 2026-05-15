package com.feature.neighbourHood_backend.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_message_conversation", columnList = "conversation_id"),
        @Index(name = "idx_chat_message_sender", columnList = "sender_id"),
        @Index(name = "idx_chat_message_sent", columnList = "sent_at")
})
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @Column(name = "sender_id", nullable = false, columnDefinition = "uuid")
    private UUID senderId;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "message_type", nullable = false)
    private String messageType;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @PrePersist
    public void prePersist() {
        this.sentAt = LocalDateTime.now();
        if (this.messageType == null || this.messageType.isBlank()) {
            this.messageType = "text";
        }
    }
}
