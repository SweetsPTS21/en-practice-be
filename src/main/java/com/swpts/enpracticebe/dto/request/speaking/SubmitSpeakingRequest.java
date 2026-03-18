package com.swpts.enpracticebe.dto.request.speaking;

import com.swpts.enpracticebe.dto.speech.SpeechAnalyticsDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitSpeakingRequest {

    @NotBlank
    private String transcript;

    private String audioUrl;

    private Integer timeSpentSeconds;

    /**
     * Speech analytics captured client-side from the STT WebSocket session.
     * Sent alongside the transcript so the server can persist and use it for grading.
     * Optional — grading still works without it (falls back to text-only AI scoring).
     */
    private SpeechAnalyticsDto speechAnalytics;
}
