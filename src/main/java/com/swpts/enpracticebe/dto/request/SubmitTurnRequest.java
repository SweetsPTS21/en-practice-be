package com.swpts.enpracticebe.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitTurnRequest {
    @NotBlank(message = "Transcript is required")
    private String transcript;

    private String audioUrl;
    private Integer timeSpentSeconds;
}
