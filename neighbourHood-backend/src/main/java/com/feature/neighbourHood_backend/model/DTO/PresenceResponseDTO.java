package com.feature.neighbourHood_backend.model.DTO;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PresenceResponseDTO {
    private String email;
    private String status;
    private LocalDateTime lastActiveAt;
}
