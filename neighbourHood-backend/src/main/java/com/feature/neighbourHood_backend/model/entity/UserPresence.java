package com.feature.neighbourHood_backend.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "user_presence", indexes = {
        @Index(name = "idx_user_presence_status", columnList = "status"),
        @Index(name = "idx_user_presence_active", columnList = "last_active_at")
})
public class UserPresence {
    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;

    @PrePersist
    public void prePersist() {
        if (status == null || status.isBlank()) {
            status = "online";
        }
        if (lastActiveAt == null) {
            lastActiveAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (lastActiveAt == null) {
            lastActiveAt = LocalDateTime.now();
        }
    }
}
