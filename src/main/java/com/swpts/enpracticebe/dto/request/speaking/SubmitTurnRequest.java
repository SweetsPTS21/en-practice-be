package com.swpts.enpracticebe.dto.request.speaking;

import com.swpts.enpracticebe.dto.speech.SpeechAnalyticsDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitTurnRequest {
    @NotBlank(message = "Transcript is required")
    private String transcript;

    private String audioUrl;
    private Integer timeSpentSeconds;

    /** Optional speech analytics from the STT session for this turn */
    private SpeechAnalyticsDto speechAnalytics;
}
