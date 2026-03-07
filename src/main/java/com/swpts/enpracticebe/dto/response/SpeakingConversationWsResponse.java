package com.swpts.enpracticebe.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class SpeakingConversationWsResponse {
    private String type; // "NEXT_QUESTION", "CONVERSATION_COMPLETE", "ERROR"
    private UUID conversationId;
    private Integer turnNumber;
    private String aiQuestion;
    private String audioBase64;
    private boolean lastTurn;
    private String errorMessage;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    public static SpeakingConversationWsResponse error(String message) {
        return SpeakingConversationWsResponse.builder()
                .type("ERROR")
                .errorMessage(message)
                .timestamp(Instant.now())
                .build();
    }
}
