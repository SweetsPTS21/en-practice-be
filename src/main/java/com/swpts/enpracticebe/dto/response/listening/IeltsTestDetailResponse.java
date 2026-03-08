package com.swpts.enpracticebe.dto.response.listening;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IeltsTestDetailResponse {
    private UUID id;
    private String title;
    private String skill;
    private Integer timeLimitMinutes;
    private String difficulty;
    private Boolean isPublished;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QuestionDto {
        private UUID id;
        private Integer questionOrder;
        private String questionType;
        private String questionText;
        private List<String> options;
        private List<String> correctAnswers;
        private String explanation;
    }
}
