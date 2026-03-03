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
public class TestAttemptHistoryResponse {
    private UUID attemptId;
    private UUID testId;
    private String testTitle;
    private String skill;
    private Integer totalQuestions;
    private Integer correctCount;
    private Float bandScore;
    private Integer timeSpentSeconds;
    private String status;
    private Instant startedAt;
    private Instant completedAt;
}
