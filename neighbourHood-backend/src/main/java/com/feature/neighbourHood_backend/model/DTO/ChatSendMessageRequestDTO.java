package com.feature.neighbourHood_backend.model.DTO;

import lombok.Data;

@Data
public class ChatSendMessageRequestDTO {
    private String peerEmail;
    private String content;
    private String messageType;
    private String attachmentUrl;
}
