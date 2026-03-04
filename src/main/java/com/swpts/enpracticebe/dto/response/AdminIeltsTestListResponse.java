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
public class AdminIeltsTestListResponse {
    private UUID id;
    private String title;
    private String skill;
    private Integer timeLimitMinutes;
    private String difficulty;
    private Boolean isPublished;
    private Integer totalQuestions;
    private Instant createdAt;
    private Instant updatedAt;
}
