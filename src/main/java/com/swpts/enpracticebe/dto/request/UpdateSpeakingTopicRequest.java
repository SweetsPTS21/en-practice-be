package com.swpts.enpracticebe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSpeakingTopicRequest {

    @NotBlank
    private String part;

    @NotBlank
    private String question;

    private String cueCard;

    private List<String> followUpQuestions;

    private String aiGradingPrompt;

    @NotBlank
    private String difficulty;

    @NotNull
    private Boolean isPublished;
}
