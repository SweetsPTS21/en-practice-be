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
public class IeltsTestListResponse {
    private UUID id;
    private String title;
    private String skill;
    private Integer timeLimitMinutes;
    private String difficulty;
    private Integer totalQuestions;
    private Instant createdAt;
}
