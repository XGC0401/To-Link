package com.feature.neighbourHood_backend.model.DTO;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentResponseDTO {
    private Long id;
    private String authorName;
    private String authorEmail;
    private String content;
    private OffsetDateTime createdAt;
}
