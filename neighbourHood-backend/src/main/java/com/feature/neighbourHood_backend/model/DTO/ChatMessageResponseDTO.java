package com.feature.neighbourHood_backend.model.DTO;

import java.time.LocalDateTime;

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
    private LocalDateTime sentAt;
    private boolean read;
}
