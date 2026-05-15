package com.feature.neighbourHood_backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.feature.neighbourHood_backend.model.entity.ChatConversation;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    Optional<ChatConversation> findByUserAIdAndUserBId(UUID userAId, UUID userBId);

    List<ChatConversation> findByUserAIdOrUserBIdOrderByUpdatedAtDesc(UUID userAId, UUID userBId);
}
