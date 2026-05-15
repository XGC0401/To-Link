package com.feature.neighbourHood_backend.model.DTO;

import java.time.OffsetDateTime;

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
    private OffsetDateTime lastMessageTime;
    private long unreadCount;
}
