package com.swpts.enpracticebe.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TtsRequest {
    @NotBlank(message = "Text is required")
    private String text;

    private String voiceName;
}
