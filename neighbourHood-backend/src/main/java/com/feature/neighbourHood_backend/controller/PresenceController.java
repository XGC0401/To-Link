package com.feature.neighbourHood_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.feature.neighbourHood_backend.model.CustomUserDetails;
import com.feature.neighbourHood_backend.model.DTO.ApiResponse;
import com.feature.neighbourHood_backend.model.DTO.PresenceLookupRequestDTO;
import com.feature.neighbourHood_backend.model.DTO.PresenceResponseDTO;
import com.feature.neighbourHood_backend.model.DTO.PresenceUpdateRequestDTO;
import com.feature.neighbourHood_backend.service.PresenceService;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/presence")
public class PresenceController {
    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<PresenceResponseDTO>> updateMyPresence(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PresenceUpdateRequestDTO request) {
        PresenceResponseDTO updated = presenceService.updatePresence(userDetails.getUser(), request.getStatus());
        return ResponseEntity.ok(new ApiResponse<>(true, updated, "updated"));
    }

    @PostMapping("/by-emails")
    public ResponseEntity<ApiResponse<List<PresenceResponseDTO>>> getByEmails(@RequestBody PresenceLookupRequestDTO request) {
        List<PresenceResponseDTO> data = presenceService.getPresencesByEmails(request.getEmails());
        return ResponseEntity.ok(new ApiResponse<>(true, data, "success"));
    }
}
