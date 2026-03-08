package com.swpts.enpracticebe.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateWritingTaskRequest {

    @NotBlank
    private String taskType;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String instruction;

    private List<String> imageUrls;

    private String aiGradingPrompt;

    @NotBlank
    private String difficulty;

    @NotNull
    private Boolean isPublished;

    private Integer timeLimitMinutes = 60;
    private Integer minWords = 150;
    private Integer maxWords = 300;
}
