package com.feature.neighbourHood_backend.model.DTO;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicUserProfileDTO {
    private UUID uuid;
    private String username;
    private String email;
    private String status;
    private String house;
    private String avatar;
    private String address1;
    private String address2;
    private String address3;
}
