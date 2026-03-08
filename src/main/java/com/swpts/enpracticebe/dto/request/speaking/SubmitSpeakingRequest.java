package com.swpts.enpracticebe.dto.request.speaking;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitSpeakingRequest {

    @NotBlank
    private String transcript;

    private String audioUrl;

    private Integer timeSpentSeconds;
}
