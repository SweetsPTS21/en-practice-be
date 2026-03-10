package com.swpts.enpracticebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPerformanceProfile {
    private List<QuestionTypeAccuracy> ieltsPerformance;
    private SkillSubScores speakingScores;
    private SkillSubScores writingScores;
    private VocabStats vocabStats;
    private Float overallIeltsBand;
    private String ieltsScoreTrend; // IMPROVING, DECLINING, STABLE
    private int totalIeltsAttempts;
    private int totalSpeakingAttempts;
    private int totalWritingAttempts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionTypeAccuracy {
        private String questionType;
        private int totalCount;
        private int correctCount;
        private float accuracyPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillSubScores {
        private Float score1; // fluency or taskResponse
        private Float score2; // lexical or coherence
        private Float score3; // grammar for both
        private Float score4; // pronunciation or lexicalResource (writing uses LexicalResource)
        private Float overallBand;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VocabStats {
        private float accuracyPercentage;
        private List<String> frequentlyWrongWords;
    }
}
