package com.feature.neighbourHood_backend.model.DTO;

import java.util.UUID;

public class BlockUserRequestDTO {
    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}