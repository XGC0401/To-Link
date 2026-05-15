package com.feature.neighbourHood_backend.model.DTO;

import java.util.List;

import lombok.Data;

@Data
public class PresenceLookupRequestDTO {
    private List<String> emails;
}
