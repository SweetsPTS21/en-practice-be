package com.swpts.enpracticebe.dto.request.speaking;

import com.swpts.enpracticebe.dto.speech.SpeechAnalyticsDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitCustomConversationTurnRequest {

    @NotBlank(message = "Transcript is required")
    private String transcript;

    private String audioUrl;

    private Integer timeSpentSeconds;

    private SpeechAnalyticsDto speechAnalytics;
}
