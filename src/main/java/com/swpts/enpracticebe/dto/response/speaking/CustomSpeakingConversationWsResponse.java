package com.swpts.enpracticebe.dto.response.speaking;

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
public class CustomSpeakingConversationWsResponse {
    private String type; // AI_MESSAGE, CONVERSATION_COMPLETE, ERROR
    private UUID conversationId;
    private String title;
    private Integer turnNumber;
    private String aiMessage;
    private String audioBase64;
    private String status;
    private Integer userTurnCount;
    private Integer maxUserTurns;
    private String errorMessage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant timestamp;

    public static CustomSpeakingConversationWsResponse error(String message) {
        return CustomSpeakingConversationWsResponse.builder()
                .type("ERROR")
                .errorMessage(message)
                .timestamp(Instant.now())
                .build();
    }
}
