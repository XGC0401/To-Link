package com.feature.neighbourHood_backend.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.feature.neighbourHood_backend.model.DTO.ChatConversationSummaryDTO;
import com.feature.neighbourHood_backend.model.DTO.ChatMessageResponseDTO;
import com.feature.neighbourHood_backend.model.DTO.ChatSendMessageRequestDTO;
import com.feature.neighbourHood_backend.model.entity.ChatConversation;
import com.feature.neighbourHood_backend.model.entity.ChatMessage;
import com.feature.neighbourHood_backend.model.entity.User;
import com.feature.neighbourHood_backend.repository.ChatConversationRepository;
import com.feature.neighbourHood_backend.repository.ChatMessageRepository;
import com.feature.neighbourHood_backend.repository.UserRepository;

@Service
public class ChatService {
    private final UserRepository userRepository;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    public ChatService(UserRepository userRepository, ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    private OffsetDateTime toUtcOffset(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atOffset(ZoneOffset.UTC);
    }

    @Transactional(readOnly = true)
    public List<ChatConversationSummaryDTO> listConversations(User currentUser) {
        List<ChatConversation> conversations = conversationRepository
                .findByUserAIdOrUserBIdOrderByUpdatedAtDesc(currentUser.getUuid(), currentUser.getUuid());
        List<ChatConversationSummaryDTO> summaries = new ArrayList<>();

        for (ChatConversation conversation : conversations) {
            UUID peerId = conversation.getUserAId().equals(currentUser.getUuid())
                    ? conversation.getUserBId()
                    : conversation.getUserAId();

            User peer = userRepository.findById(peerId).orElse(null);
            if (peer == null) {
                continue;
            }

            ChatMessage lastMessage = messageRepository.findTopByConversation_IdOrderByIdDesc(conversation.getId());
            String lastContent = lastMessage == null ? "" : lastMessage.getContent();
            String lastType = lastMessage == null ? "text" : lastMessage.getMessageType();
            LocalDateTime lastSent = lastMessage == null ? conversation.getUpdatedAt() : lastMessage.getSentAt();
            long unread = messageRepository.countByConversation_IdAndSenderIdNotAndReadFalse(conversation.getId(),
                    currentUser.getUuid());

            summaries.add(ChatConversationSummaryDTO.builder()
                    .conversationId(conversation.getId())
                    .peerEmail(peer.getEmail())
                    .peerName(peer.getUsername())
                    .lastMessage(lastContent)
                    .lastMessageType(lastType)
                    .lastMessageTime(toUtcOffset(lastSent))
                    .unreadCount(unread)
                    .build());
        }

        return summaries;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getMessages(User currentUser, String peerEmail, Long sinceId) {
        Optional<User> peerOptional = findPeerByEmail(peerEmail);
        if (peerOptional.isEmpty()) {
            return List.of();
        }
        User peer = peerOptional.get();

        ChatConversation conversation = findConversation(currentUser.getUuid(), peer.getUuid()).orElse(null);
        if (conversation == null) {
            return List.of();
        }

        List<ChatMessage> messages = sinceId == null
                ? messageRepository.findByConversation_IdOrderByIdAsc(conversation.getId())
                : messageRepository.findByConversation_IdAndIdGreaterThanOrderByIdAsc(conversation.getId(), sinceId);

        return messages.stream().map(message -> toResponse(message, currentUser, peer)).toList();
    }

    @Transactional
    public ChatMessageResponseDTO sendMessage(User sender, String peerEmail, ChatSendMessageRequestDTO request) {
        User peer = findPeerByEmail(peerEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("Message content is required");
        }

        ChatConversation conversation = getOrCreateConversation(sender.getUuid(), peer.getUuid());

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSenderId(sender.getUuid());
        message.setContent(request.getContent().trim());
        message.setMessageType(request.getMessageType());
        message.setAttachmentUrl(request.getAttachmentUrl());
        message.setRead(false);

        ChatMessage saved = messageRepository.save(message);
        conversation.setUpdatedAt(saved.getSentAt());
        conversationRepository.save(conversation);

        return toResponse(saved, sender, peer);
    }

    @Transactional
    public int markConversationRead(User currentUser, String peerEmail) {
        Optional<User> peerOptional = findPeerByEmail(peerEmail);
        if (peerOptional.isEmpty()) {
            return 0;
        }
        User peer = peerOptional.get();

        ChatConversation conversation = findConversation(currentUser.getUuid(), peer.getUuid())
                .orElse(null);
        if (conversation == null) {
            return 0;
        }

        return messageRepository.markConversationRead(conversation.getId(), currentUser.getUuid());
    }

    private ChatMessageResponseDTO toResponse(ChatMessage message, User currentUser, User peer) {
        boolean isMe = message.getSenderId().equals(currentUser.getUuid());
        return ChatMessageResponseDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderEmail(isMe ? currentUser.getEmail() : peer.getEmail())
                .senderName(isMe ? currentUser.getUsername() : peer.getUsername())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .attachmentUrl(message.getAttachmentUrl())
            .sentAt(toUtcOffset(message.getSentAt()))
                .read(message.isRead())
                .build();
    }

    private ChatConversation getOrCreateConversation(UUID userOneId, UUID userTwoId) {
        UUID first = sortFirst(userOneId, userTwoId);
        UUID second = sortSecond(userOneId, userTwoId);

        return conversationRepository.findByUserAIdAndUserBId(first, second)
                .orElseGet(() -> {
                    ChatConversation newConversation = new ChatConversation();
                    newConversation.setUserAId(first);
                    newConversation.setUserBId(second);
                    return conversationRepository.save(newConversation);
                });
    }

    private Optional<ChatConversation> findConversation(UUID userOneId, UUID userTwoId) {
        UUID first = sortFirst(userOneId, userTwoId);
        UUID second = sortSecond(userOneId, userTwoId);
        return conversationRepository.findByUserAIdAndUserBId(first, second);
    }

    private Optional<User> findPeerByEmail(String peerEmail) {
        if (!StringUtils.hasText(peerEmail)) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(peerEmail.trim());
    }

    private UUID sortFirst(UUID a, UUID b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    private UUID sortSecond(UUID a, UUID b) {
        return a.compareTo(b) <= 0 ? b : a;
    }
}
