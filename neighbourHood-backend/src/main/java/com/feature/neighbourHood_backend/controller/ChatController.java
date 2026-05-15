package com.feature.neighbourHood_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.feature.neighbourHood_backend.model.CustomUserDetails;
import com.feature.neighbourHood_backend.model.DTO.ApiResponse;
import com.feature.neighbourHood_backend.model.DTO.ChatConversationSummaryDTO;
import com.feature.neighbourHood_backend.model.DTO.ChatMessageResponseDTO;
import com.feature.neighbourHood_backend.model.DTO.ChatSendMessageRequestDTO;
import com.feature.neighbourHood_backend.service.ChatService;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ChatConversationSummaryDTO>>> listConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ChatConversationSummaryDTO> data = chatService.listConversations(userDetails.getUser());
        return ResponseEntity.ok(new ApiResponse<>(true, data, "success"));
    }

    @GetMapping("/messages/{peerEmail}")
    public ResponseEntity<ApiResponse<List<ChatMessageResponseDTO>>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String peerEmail,
            @RequestParam(required = false) Long sinceId) {
        List<ChatMessageResponseDTO> data = chatService.getMessages(userDetails.getUser(), peerEmail, sinceId);
        return ResponseEntity.ok(new ApiResponse<>(true, data, "success"));
    }

    @PostMapping("/messages/{peerEmail}")
    public ResponseEntity<ApiResponse<ChatMessageResponseDTO>> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String peerEmail,
            @RequestBody ChatSendMessageRequestDTO request) {
        ChatMessageResponseDTO data = chatService.sendMessage(userDetails.getUser(), peerEmail, request);
        return ResponseEntity.ok(new ApiResponse<>(true, data, "sent"));
    }

    @PostMapping("/read/{peerEmail}")
    public ResponseEntity<ApiResponse<Integer>> markConversationRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String peerEmail) {
        int updated = chatService.markConversationRead(userDetails.getUser(), peerEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, updated, "updated"));
    }
}
