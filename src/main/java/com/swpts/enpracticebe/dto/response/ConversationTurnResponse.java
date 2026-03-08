package com.swpts.enpracticebe.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ConversationTurnResponse {
    private UUID id;
    private Integer turnNumber;
    private String aiQuestion;
    private String userTranscript;
    private String audioUrl;
    private String turnType;
    private Integer timeSpentSeconds;
    private Instant createdAt;
}
