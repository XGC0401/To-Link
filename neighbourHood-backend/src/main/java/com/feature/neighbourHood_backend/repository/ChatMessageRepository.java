package com.feature.neighbourHood_backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.feature.neighbourHood_backend.model.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversation_IdOrderByIdAsc(Long conversationId);

    List<ChatMessage> findByConversation_IdAndIdGreaterThanOrderByIdAsc(Long conversationId, Long id);

    ChatMessage findTopByConversation_IdOrderByIdDesc(Long conversationId);

    long countByConversation_IdAndSenderIdNotAndReadFalse(Long conversationId, UUID senderId);

    @Modifying
    @Query("update ChatMessage m set m.read = true where m.conversation.id = :conversationId and m.senderId <> :viewerId and m.read = false")
    int markConversationRead(@Param("conversationId") Long conversationId, @Param("viewerId") UUID viewerId);
}
