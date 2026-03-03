package com.swpts.enpracticebe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IeltsTestDetailResponse {
    private UUID id;
    private String title;
    private String skill;
    private Integer timeLimitMinutes;
    private String difficulty;
    private List<SectionDto> sections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionDto {
        private UUID id;
        private Integer sectionOrder;
        private String title;
        private String audioUrl;
        private String instructions;
        private List<PassageDto> passages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassageDto {
        private UUID id;
        private Integer passageOrder;
        private String title;
        private String content;
        private List<QuestionDto> questions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDto {
        private UUID id;
        private Integer questionOrder;
        private String questionType;
        private String questionText;
        private List<String> options;
        // NOTE: correctAnswers is intentionally excluded for test-taking
    }
}
