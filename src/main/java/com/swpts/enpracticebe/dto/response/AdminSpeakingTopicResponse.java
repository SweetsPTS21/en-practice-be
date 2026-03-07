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
public class AdminSpeakingTopicResponse {
    private UUID id;
    private String part;
    private String question;
    private String cueCard;
    private List<String> followUpQuestions;
    private String aiGradingPrompt;
    private String difficulty;
    private Boolean isPublished;
    private Instant createdAt;
    private Instant updatedAt;
}
