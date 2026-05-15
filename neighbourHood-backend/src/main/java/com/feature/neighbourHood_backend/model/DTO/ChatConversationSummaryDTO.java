package com.feature.neighbourHood_backend.model.DTO;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatConversationSummaryDTO {
    private Long conversationId;
    private String peerEmail;
    private String peerName;
    private String lastMessage;
    private String lastMessageType;
    private LocalDateTime lastMessageTime;
    private long unreadCount;
}
