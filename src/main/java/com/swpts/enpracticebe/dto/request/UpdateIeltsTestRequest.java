package com.swpts.enpracticebe.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateIeltsTestRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String skill;

    private Integer timeLimitMinutes = 60;

    private String difficulty = "MEDIUM";

    private Boolean isPublished = false;

    @NotNull
    @Valid
    private List<CreateIeltsTestRequest.SectionRequest> sections;
}
