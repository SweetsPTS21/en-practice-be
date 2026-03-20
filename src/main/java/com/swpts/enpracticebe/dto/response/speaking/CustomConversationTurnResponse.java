package com.swpts.enpracticebe.dto.response.speaking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swpts.enpracticebe.dto.speech.SpeechAnalyticsDto;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CustomConversationTurnResponse {
    private UUID id;
    private Integer turnNumber;
    private String aiMessage;
    private String userTranscript;
    private String audioUrl;
    private Integer timeSpentSeconds;
    private SpeechAnalyticsDto speechAnalytics;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;
}
