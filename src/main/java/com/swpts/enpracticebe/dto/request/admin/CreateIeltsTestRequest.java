package com.swpts.enpracticebe.dto.request.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateIeltsTestRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String skill; // LISTENING, READING

    private Integer timeLimitMinutes = 60;

    private String difficulty = "MEDIUM"; // EASY, MEDIUM, HARD

    private Boolean isPublished = false;

    @NotNull
    @Valid
    private List<SectionRequest> sections;

    @Data
    public static class SectionRequest {
        @NotNull
        private Integer sectionOrder;
        private String title;
        private String audioUrl;
        private String instructions;

        @NotNull
        @Valid
        private List<PassageRequest> passages;
    }

    @Data
    public static class PassageRequest {
        @NotNull
        private Integer passageOrder;
        private String title;
        private String content;

        @NotNull
        @Valid
        private List<QuestionRequest> questions;
    }

    @Data
    public static class QuestionRequest {
        @NotNull
        private Integer questionOrder;

        @NotBlank
        private String questionType;

        @NotBlank
        private String questionText;

        private List<String> options;

        @NotNull
        private List<String> correctAnswers;

        private String explanation;
    }
}
