package com.swpts.enpracticebe.dto.response.listening;

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
public class SubmitTestResponse {
    private UUID attemptId;
    private Integer totalQuestions;
    private Integer correctCount;
    private Float bandScore;
    private Integer timeSpentSeconds;
    private List<AnswerResultItem> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerResultItem {
        private UUID questionId;
        private String questionText;
        private String questionType;
        private List<String> userAnswer;
        private List<String> correctAnswer;
        private Boolean isCorrect;
        private String explanation;
    }
}
