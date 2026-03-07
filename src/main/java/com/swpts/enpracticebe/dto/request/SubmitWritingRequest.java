package com.swpts.enpracticebe.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitWritingRequest {

    @NotBlank
    private String essayContent;

    private Integer timeSpentSeconds;
}
