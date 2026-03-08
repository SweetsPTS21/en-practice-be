package com.swpts.enpracticebe.dto.request.writing;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitWritingRequest {

    @NotBlank
    private String essayContent;

    private Integer timeSpentSeconds;
}
