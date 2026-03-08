package com.swpts.enpracticebe.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NextQuestionResponse {
    private UUID conversationId;
    private Integer turnNumber;
    private String aiQuestion;
    private String turnType; // QUESTION or HINT
    private boolean lastTurn;
    private boolean conversationComplete;
}
