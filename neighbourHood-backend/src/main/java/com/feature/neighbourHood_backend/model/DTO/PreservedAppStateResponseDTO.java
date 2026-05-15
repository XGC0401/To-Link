package com.feature.neighbourHood_backend.model.DTO;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreservedAppStateResponseDTO {
    private Map<String, String> state;
}
