package com.feature.neighbourHood_backend.model.DTO;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageResponseDTO {
    private Long id;
    private Long conversationId;
    private String senderEmail;
    private String senderName;
    private String content;
    private String messageType;
    private String attachmentUrl;
    private OffsetDateTime sentAt;
    private boolean read;
}
