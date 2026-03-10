package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.response.ai.AiAskResponse;
import com.swpts.enpracticebe.dto.response.dashboard.AiRecommendation;
import com.swpts.enpracticebe.dto.response.dashboard.RecommendedPractice;
import com.swpts.enpracticebe.dto.response.dashboard.UserPerformanceProfile;
import com.swpts.enpracticebe.entity.IeltsTestAttempt;
import com.swpts.enpracticebe.entity.SpeakingAttempt;
import com.swpts.enpracticebe.entity.WritingSubmission;
import com.swpts.enpracticebe.repository.*;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.service.UserStatsAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatsAggregatorServiceImpl implements UserStatsAggregatorService {

    private final IeltsTestAttemptRepository ieltsTestAttemptRepository;
    private final IeltsAnswerRecordRepository ieltsAnswerRecordRepository;
    private final SpeakingAttemptRepository speakingAttemptRepository;
    private final WritingSubmissionRepository writingSubmissionRepository;
    private final VocabularyRecordRepository vocabularyRecordRepository;
    private final OpenClawService openClawService;
    private final ObjectMapper objectMapper;

    @Override
    public UserPerformanceProfile buildPerformanceProfile(UUID userId) {
        CompletableFuture<List<UserPerformanceProfile.QuestionTypeAccuracy>> ieltsFuture = CompletableFuture.supplyAsync(() -> {
            List<Object[]> raw = ieltsAnswerRecordRepository.findQuestionTypeAccuracy(userId);
            return raw.stream().map(row -> {
                String qType = String.valueOf(row[0]);
                int total = ((Number) row[1]).intValue();
                int correct = ((Number) row[2]).intValue();
                float acc = total > 0 ? (float) correct / total * 100 : 0;
                return new UserPerformanceProfile.QuestionTypeAccuracy(qType, total, correct, acc);
            }).toList();
        });

        CompletableFuture<UserPerformanceProfile.SkillSubScores> speakingFuture = CompletableFuture.supplyAsync(() -> {
            List<SpeakingAttempt> attempts = speakingAttemptRepository.findTop10ByUserIdAndStatusOrderByGradedAtDesc(userId, SpeakingAttempt.AttemptStatus.GRADED);
            if (attempts.isEmpty()) return null;
            
            float fluency = 0, lexical = 0, grammar = 0, pronunciation = 0, overall = 0;
            int count = attempts.size();
            for (SpeakingAttempt a : attempts) {
                fluency += a.getFluencyScore() != null ? a.getFluencyScore() : 0;
                lexical += a.getLexicalScore() != null ? a.getLexicalScore() : 0;
                grammar += a.getGrammarScore() != null ? a.getGrammarScore() : 0;
                pronunciation += a.getPronunciationScore() != null ? a.getPronunciationScore() : 0;
                overall += a.getOverallBandScore() != null ? a.getOverallBandScore() : 0;
            }
            return new UserPerformanceProfile.SkillSubScores(
                    fluency / count, lexical / count, grammar / count, pronunciation / count, overall / count
            );
        });

        CompletableFuture<UserPerformanceProfile.SkillSubScores> writingFuture = CompletableFuture.supplyAsync(() -> {
            List<WritingSubmission> attempts = writingSubmissionRepository.findTop10ByUserIdAndStatusOrderByGradedAtDesc(userId, WritingSubmission.SubmissionStatus.GRADED);
            if (attempts.isEmpty()) return null;

            float taskResponse = 0, coherence = 0, lexical = 0, grammar = 0, overall = 0;
            int count = attempts.size();
            for (WritingSubmission w : attempts) {
                taskResponse += w.getTaskResponseScore() != null ? w.getTaskResponseScore() : 0;
                coherence += w.getCoherenceScore() != null ? w.getCoherenceScore() : 0;
                lexical += w.getLexicalResourceScore() != null ? w.getLexicalResourceScore() : 0;
                grammar += w.getGrammarScore() != null ? w.getGrammarScore() : 0;
                overall += w.getOverallBandScore() != null ? w.getOverallBandScore() : 0;
            }
            return new UserPerformanceProfile.SkillSubScores(
                    taskResponse / count, coherence / count, grammar / count, lexical / count, overall / count
            );
        });

        CompletableFuture<UserPerformanceProfile.VocabStats> vocabFuture = CompletableFuture.supplyAsync(() -> {
            long total = vocabularyRecordRepository.countAllUniqueWords(userId);
            if (total == 0) return null;
            long correct = vocabularyRecordRepository.countUniqueWordsSince(userId, java.time.Instant.EPOCH) - vocabularyRecordRepository.countUniqueWrongWords(userId); // Approximation
            float acc = (float) Math.max(0, correct) / total * 100;

            List<Object[]> rawWrong = vocabularyRecordRepository.findFrequentlyWrongWords(userId);
            List<String> wrongWords = rawWrong.stream()
                    .map(row -> String.valueOf(row[0]))
                    .limit(5)
                    .toList();

            return new UserPerformanceProfile.VocabStats(acc, wrongWords);
        });

        CompletableFuture<List<IeltsTestAttempt>> recentIeltsFuture = CompletableFuture.supplyAsync(() -> {
            List<IeltsTestAttempt> attempts = ieltsTestAttemptRepository.findByUserIdOrderByStartedAtDesc(userId);
            return attempts.stream()
                    .filter(a -> a.getStatus() == IeltsTestAttempt.AttemptStatus.COMPLETED && a.getBandScore() != null)
                    .limit(5)
                    .toList();
        });
        
        CompletableFuture<Integer> totalIeltsAttempts = CompletableFuture.supplyAsync(() -> (int) ieltsTestAttemptRepository.countByUserId(userId));
        CompletableFuture<Integer> totalSpeakingAttempts = CompletableFuture.supplyAsync(() -> (int) speakingAttemptRepository.countByUserId(userId));
        CompletableFuture<Integer> totalWritingAttempts = CompletableFuture.supplyAsync(() -> (int) writingSubmissionRepository.countByUserId(userId));

        CompletableFuture.allOf(ieltsFuture, speakingFuture, writingFuture, vocabFuture, 
                                recentIeltsFuture, totalIeltsAttempts, totalSpeakingAttempts, totalWritingAttempts).join();

        // Calculate IELTS trend
        List<IeltsTestAttempt> recentIelts = recentIeltsFuture.join();
        String trend = "STABLE";
        Float overallIeltsBand = null;
        if (!recentIelts.isEmpty()) {
            overallIeltsBand = recentIelts.get(0).getBandScore();
            if (recentIelts.size() >= 2) {
                float first = recentIelts.get(0).getBandScore();
                float last = recentIelts.get(recentIelts.size() - 1).getBandScore();
                if (first > last) trend = "IMPROVING";
                else if (first < last) trend = "DECLINING";
            }
        }

        return UserPerformanceProfile.builder()
                .ieltsPerformance(ieltsFuture.join())
                .speakingScores(speakingFuture.join())
                .writingScores(writingFuture.join())
                .vocabStats(vocabFuture.join())
                .overallIeltsBand(overallIeltsBand)
                .ieltsScoreTrend(trend)
                .totalIeltsAttempts(totalIeltsAttempts.join())
                .totalSpeakingAttempts(totalSpeakingAttempts.join())
                .totalWritingAttempts(totalWritingAttempts.join())
                .build();
    }

    @Override
    @Cacheable(value = "userWeakSkills", key = "#userId", unless = "#result == null or #result.isEmpty()")
    public List<String> getWeakSkills(UUID userId) {
        return getWeakSkillsFromProfile(buildPerformanceProfile(userId));
    }

    @Override
    public List<String> getWeakSkillsFromProfile(UserPerformanceProfile profile) {
        List<String> weakSkills = new ArrayList<>();

        if (profile.getIeltsPerformance() != null) {
            weakSkills.addAll(profile.getIeltsPerformance().stream()
                    .filter(q -> q.getAccuracyPercentage() < 60 && q.getTotalCount() > 5)
                    .map(UserPerformanceProfile.QuestionTypeAccuracy::getQuestionType)
                    .limit(2)
                    .toList());
        }

        if (profile.getSpeakingScores() != null) {
            UserPerformanceProfile.SkillSubScores s = profile.getSpeakingScores();
            if (s.getScore1() != null && s.getScore1() < 6.0) weakSkills.add("Speaking Fluency");
            if (s.getScore3() != null && s.getScore3() < 6.0) weakSkills.add("Speaking Grammar");
        }

        if (profile.getWritingScores() != null) {
            UserPerformanceProfile.SkillSubScores w = profile.getWritingScores();
            if (w.getScore1() != null && w.getScore1() < 6.0) weakSkills.add("Writing Task Response");
            if (w.getScore3() != null && w.getScore3() < 6.0) weakSkills.add("Writing Grammar");
        }

        if (weakSkills.isEmpty()) {
            weakSkills.add("Academic Vocabulary");
            weakSkills.add("Map Labeling");
        }

        return weakSkills;
    }

    @Override
    @Cacheable(value = "recommendedPractice", key = "#userId", unless = "#result == null or #result.isEmpty()")
    public List<RecommendedPractice> getRecommendedPractice(UUID userId) {
        return getRecommendedPracticeFromProfile(buildPerformanceProfile(userId), userId);
    }

    @Override
    public List<RecommendedPractice> getRecommendedPracticeFromProfile(UserPerformanceProfile profile, UUID userId) {
        boolean hasData = profile.getTotalIeltsAttempts() > 0 || profile.getTotalSpeakingAttempts() > 0 || profile.getTotalWritingAttempts() > 0;

        if (hasData) {
            try {
                String prompt = buildAiPrompt(profile);
                AiAskResponse response = openClawService.askAi(prompt, userId);
                List<AiRecommendation> aiRecs = parseAiResponse(response.getAnswer());

                if (aiRecs != null && !aiRecs.isEmpty()) {
                    return mapToRecommendedPractices(aiRecs);
                }
            } catch (Exception e) {
                log.error("Failed to generate AI recommendations, falling back to rule-based", e);
            }
        }
        
        return getFallbackRecommendations(profile);
    }

    private String buildAiPrompt(UserPerformanceProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an IELTS tutor analyzing a student's practice history. ");
        sb.append("Based on the following performance profile, provide 3 specific, actionable practice recommendations. ");
        sb.append("Return ONLY a JSON array, nothing else.\\n\\n");
        
        try {
            sb.append("## Student Performance Profile:\\n");
            sb.append(objectMapper.writeValueAsString(profile));
        } catch (JsonProcessingException e) {
            sb.append("Data serialization failed.");
        }
        
        sb.append("\\n\\n## Instructions:\\n");
        sb.append("Return ONLY a JSON array with this format:\\n");
        sb.append("[\\n  {\\n");
        sb.append("    \"title\": \"...\",\\n");
        sb.append("    \"description\": \"...\",\\n");
        sb.append("    \"type\": \"LISTENING|READING|SPEAKING|WRITING|VOCAB\",\\n");
        sb.append("    \"difficulty\": \"Easy|Medium|Hard\",\\n");
        sb.append("    \"estimatedTime\": \"X mins\",\\n");
        sb.append("    \"reason\": \"short explanation why this is recommended\",\\n");
        sb.append("    \"priority\": 1-3\\n");
        sb.append("  }\\n]\\n");
        sb.append("Sort by priority (1 = highest). Be specific and encouraging.");
        
        return sb.toString();
    }

    private List<AiRecommendation> parseAiResponse(String content) {
        try {
            // Clean up typical markdown formatting from LLM
            String jsonContent = content;
            if (content.startsWith("```json")) {
                jsonContent = content.substring(7);
                if (jsonContent.endsWith("```")) {
                    jsonContent = jsonContent.substring(0, jsonContent.length() - 3);
                }
            } else if (content.startsWith("```")) {
                jsonContent = content.substring(3);
                if (jsonContent.endsWith("```")) {
                    jsonContent = jsonContent.substring(0, jsonContent.length() - 3);
                }
            }
            return objectMapper.readValue(jsonContent, new TypeReference<List<AiRecommendation>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse AI recommendation JSON: {}", content);
            return null;
        }
    }

    private List<RecommendedPractice> mapToRecommendedPractices(List<AiRecommendation> aiRecs) {
        return aiRecs.stream().map(ai -> {
            String path = "/";
            if ("LISTENING".equalsIgnoreCase(ai.getType()) || "READING".equalsIgnoreCase(ai.getType())) path = "/ielts";
            else if ("SPEAKING".equalsIgnoreCase(ai.getType())) path = "/speaking";
            else if ("WRITING".equalsIgnoreCase(ai.getType())) path = "/writing";
            
            return RecommendedPractice.builder()
                    .id(UUID.randomUUID().toString())
                    .title(ai.getTitle())
                    .description(ai.getDescription())
                    .type(ai.getType())
                    .difficulty(ai.getDifficulty())
                    .estimatedTime(ai.getEstimatedTime())
                    .reason(ai.getReason())
                    .priority(ai.getPriority())
                    .path(path)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<RecommendedPractice> getFallbackRecommendations(UserPerformanceProfile profile) {
        List<RecommendedPractice> recommendations = new ArrayList<>();
        
        boolean isNewUser = profile.getTotalIeltsAttempts() == 0 && profile.getTotalSpeakingAttempts() == 0;
        
        if (isNewUser) {
            recommendations.add(RecommendedPractice.builder()
                .id("r1")
                .title("Diagnostic Test")
                .description("Take a short Listening test to assess your baseline.")
                .type("LISTENING")
                .difficulty("Medium")
                .estimatedTime("15 mins")
                .path("/ielts")
                .reason("We need a baseline to personalize your learning path.")
                .priority(1)
                .build());
                
            recommendations.add(RecommendedPractice.builder()
                .id("r2")
                .title("Speak Your Mind")
                .description("Try answering a Part 1 speaking question.")
                .type("SPEAKING")
                .difficulty("Easy")
                .estimatedTime("5 mins")
                .path("/speaking")
                .reason("Get comfortable with the speaking interface.")
                .priority(2)
                .build());
        } else {
            // General fallbacks
            recommendations.add(RecommendedPractice.builder()
                .id("r3")
                .title("Daily Vocabulary")
                .description("Review 10 words to keep your memory fresh.")
                .type("VOCAB")
                .difficulty("Medium")
                .estimatedTime("5 mins")
                .path("/")
                .reason("Consistent vocabulary review is key to improvement.")
                .priority(1)
                .build());
        }
        return recommendations;
    }
}
