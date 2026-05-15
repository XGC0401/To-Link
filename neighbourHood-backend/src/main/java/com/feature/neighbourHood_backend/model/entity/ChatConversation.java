package com.feature.neighbourHood_backend.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "chat_conversations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_chat_conversation_users", columnNames = { "user_a_id", "user_b_id" })
}, indexes = {
        @Index(name = "idx_chat_conversation_user_a", columnList = "user_a_id"),
        @Index(name = "idx_chat_conversation_user_b", columnList = "user_b_id"),
        @Index(name = "idx_chat_conversation_updated", columnList = "updated_at")
})
public class ChatConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_a_id", nullable = false, columnDefinition = "uuid")
    private UUID userAId;

    @Column(name = "user_b_id", nullable = false, columnDefinition = "uuid")
    private UUID userBId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
