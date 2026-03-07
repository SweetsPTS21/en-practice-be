package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.response.AiAskResponse;
import com.swpts.enpracticebe.entity.SpeakingAttempt;
import com.swpts.enpracticebe.entity.SpeakingTopic;
import com.swpts.enpracticebe.repository.SpeakingAttemptRepository;
import com.swpts.enpracticebe.repository.SpeakingTopicRepository;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.service.PushNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Separate bean for @Async to work via Spring AOP proxy.
 */
@Slf4j
@Service
public class SpeakingGradingService {

    private final SpeakingAttemptRepository attemptRepository;
    private final SpeakingTopicRepository topicRepository;
    private final OpenClawService openClawService;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper;

    public SpeakingGradingService(SpeakingAttemptRepository attemptRepository,
            SpeakingTopicRepository topicRepository,
            OpenClawService openClawService,
            PushNotificationService pushNotificationService,
            ObjectMapper objectMapper) {
        this.attemptRepository = attemptRepository;
        this.topicRepository = topicRepository;
        this.openClawService = openClawService;
        this.pushNotificationService = pushNotificationService;
        this.objectMapper = objectMapper;
    }

    @Async("aiGradingExecutor")
    public void gradeAttemptAsync(UUID attemptId, UUID userId) {
        try {
            SpeakingAttempt attempt = attemptRepository.findById(attemptId)
                    .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

            SpeakingTopic topic = topicRepository.findById(attempt.getTopicId())
                    .orElseThrow(() -> new RuntimeException("Topic not found: " + attempt.getTopicId()));

            attempt.setStatus(SpeakingAttempt.AttemptStatus.GRADING);
            attemptRepository.save(attempt);

            String gradingPrompt = buildGradingPrompt(topic, attempt);
            AiAskResponse aiResponse = openClawService.askAi(gradingPrompt, userId);
            parseAndSaveGradingResult(attempt, aiResponse.getAnswer());

            try {
                pushNotificationService.sendNotificationToUser(
                        userId,
                        "Speaking Graded",
                        "Your IELTS Speaking attempt has been graded. Band: " + attempt.getOverallBandScore());
            } catch (Exception e) {
                log.warn("Failed to send push notification to user {}: {}", userId, e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error grading speaking attempt {}: {}", attemptId, e.getMessage(), e);
            try {
                SpeakingAttempt attempt = attemptRepository.findById(attemptId).orElse(null);
                if (attempt != null) {
                    attempt.setStatus(SpeakingAttempt.AttemptStatus.FAILED);
                    attempt.setAiFeedback("AI grading failed: " + e.getMessage());
                    attemptRepository.save(attempt);
                }
            } catch (Exception ex) {
                log.error("Failed to update attempt status to FAILED: {}", ex.getMessage());
            }
        }
    }

    private String buildGradingPrompt(SpeakingTopic topic, SpeakingAttempt attempt) {
        String customPrompt = topic.getAiGradingPrompt();
        if (customPrompt != null && !customPrompt.isBlank()) {
            return customPrompt
                    .replace("{transcript}", attempt.getTranscript())
                    .replace("{question}", topic.getQuestion())
                    .replace("{part}", topic.getPart().name());
        }

        String partDesc = switch (topic.getPart()) {
            case PART_1 -> "IELTS Speaking Part 1 (familiar topics, short answers)";
            case PART_2 -> "IELTS Speaking Part 2 (individual long turn / cue card)";
            case PART_3 -> "IELTS Speaking Part 3 (discussion, abstract ideas)";
        };

        String cueCardSection = topic.getCueCard() != null && !topic.getCueCard().isBlank()
                ? "\n\n**Cue Card:**\n" + topic.getCueCard()
                : "";

        return String.format("""
                You are an IELTS Speaking examiner. Grade the following %s response.

                **Question:**
                %s%s

                **Student's Transcript:**
                %s

                Grade on these 4 criteria (each 0.0 to 9.0, in 0.5 increments):
                1. Fluency and Coherence
                2. Lexical Resource
                3. Grammatical Range and Accuracy
                4. Pronunciation

                You MUST respond in the following JSON format only, no extra text:
                {
                  "fluency": 6.5,
                  "lexical": 6.0,
                  "grammar": 6.5,
                  "pronunciation": 6.0,
                  "overall_band": 6.5,
                  "feedback": "Your detailed feedback in markdown format here..."
                }
                """,
                partDesc,
                topic.getQuestion(),
                cueCardSection,
                attempt.getTranscript());
    }

    private void parseAndSaveGradingResult(SpeakingAttempt attempt, String aiAnswer) {
        try {
            String jsonStr = extractJson(aiAnswer);
            JsonNode node = objectMapper.readTree(jsonStr);

            attempt.setFluencyScore(getFloatField(node, "fluency"));
            attempt.setLexicalScore(getFloatField(node, "lexical"));
            attempt.setGrammarScore(getFloatField(node, "grammar"));
            attempt.setPronunciationScore(getFloatField(node, "pronunciation"));
            attempt.setOverallBandScore(getFloatField(node, "overall_band"));

            String feedback = node.has("feedback") ? node.get("feedback").asText() : aiAnswer;
            attempt.setAiFeedback(feedback);
            attempt.setStatus(SpeakingAttempt.AttemptStatus.GRADED);
            attempt.setGradedAt(Instant.now());

        } catch (Exception e) {
            log.warn("Failed to parse AI grading JSON, saving raw response: {}", e.getMessage());
            attempt.setAiFeedback(aiAnswer);
            attempt.setStatus(SpeakingAttempt.AttemptStatus.GRADED);
            attempt.setGradedAt(Instant.now());
        }

        attemptRepository.save(attempt);
    }

    private String extractJson(String text) {
        if (text.contains("```json")) {
            int start = text.indexOf("```json") + 7;
            int end = text.indexOf("```", start);
            if (end > start) return text.substring(start, end).trim();
        }
        if (text.contains("```")) {
            int start = text.indexOf("```") + 3;
            int end = text.indexOf("```", start);
            if (end > start) return text.substring(start, end).trim();
        }
        int braceStart = text.indexOf('{');
        int braceEnd = text.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return text.substring(braceStart, braceEnd + 1);
        }
        return text;
    }

    private Float getFloatField(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return (float) node.get(field).asDouble();
        }
        return null;
    }
}
