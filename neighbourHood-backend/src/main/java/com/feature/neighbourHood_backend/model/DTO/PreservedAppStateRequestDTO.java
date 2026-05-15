package com.feature.neighbourHood_backend.model.DTO;

import java.util.Map;

import lombok.Data;

@Data
public class PreservedAppStateRequestDTO {
    private Map<String, String> state;
}
