package com.feature.neighbourHood_backend.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.feature.neighbourHood_backend.model.DTO.ChatSendMessageRequestDTO;
import com.feature.neighbourHood_backend.model.entity.User;
import com.feature.neighbourHood_backend.security.CustomUserDetails;
import com.feature.neighbourHood_backend.service.ChatService;

@Controller
public class ChatWebSocketController {
    
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sendMessage")
    public void handleChatMessage(ChatSendMessageRequestDTO request, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        try {
            var messageResponse = chatService.sendMessage(currentUser, request.getPeerEmail(), request);

            // Send message to the sender
            messagingTemplate.convertAndSendToUser(
                    currentUser.getEmail(),
                    "/queue/messages",
                    messageResponse
            );

            // Send message to the peer (receiver)
            messagingTemplate.convertAndSendToUser(
                    request.getPeerEmail(),
                    "/queue/messages",
                    messageResponse
            );

            // Notify both users about conversation update
            messagingTemplate.convertAndSendToUser(
                    currentUser.getEmail(),
                    "/queue/conversations",
                    messageResponse
            );
            messagingTemplate.convertAndSendToUser(
                    request.getPeerEmail(),
                    "/queue/conversations",
                    messageResponse
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                    currentUser.getEmail(),
                    "/queue/errors",
                    "Failed to send message: " + e.getMessage()
            );
        }
    }
}
