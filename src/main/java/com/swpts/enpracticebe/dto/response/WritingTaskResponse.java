package com.swpts.enpracticebe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingTaskResponse {
    private UUID id;
    private String taskType;
    private String title;
    private String content;
    private String instruction;
    private List<String> imageUrls;
    private String difficulty;
    private Boolean isPublished;
    private Integer timeLimitMinutes;
    private Integer minWords;
    private Integer maxWords;
    private Instant createdAt;
    private Instant updatedAt;
}
