package com.swpts.enpracticebe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingTaskListResponse {
    private UUID id;
    private String taskType;
    private String title;
    private String difficulty;
    private Integer timeLimitMinutes;
    private Integer minWords;
    private Integer maxWords;
    private Instant createdAt;
}
