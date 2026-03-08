package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.response.ai.AiAskResponse;
import com.swpts.enpracticebe.entity.SpeakingAttempt;
import com.swpts.enpracticebe.entity.SpeakingTopic;
import com.swpts.enpracticebe.repository.SpeakingAttemptRepository;
import com.swpts.enpracticebe.repository.SpeakingTopicRepository;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.service.PushNotificationService;
import com.swpts.enpracticebe.util.JsonUtil;
import com.swpts.enpracticebe.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SpeakingGradingService {

    private final SpeakingAttemptRepository attemptRepository;
    private final SpeakingTopicRepository topicRepository;
    private final OpenClawService openClawService;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper;

    @Async("aiGradingExecutor")
    public void gradeAttemptAsync(UUID attemptId, UUID userId) {
        try {
            SpeakingAttempt attempt = attemptRepository.findById(attemptId)
                    .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

            SpeakingTopic topic = topicRepository.findById(attempt.getTopicId())
                    .orElseThrow(() -> new RuntimeException("Topic not found: " + attempt.getTopicId()));

            attempt.setStatus(SpeakingAttempt.AttemptStatus.GRADING);
            attemptRepository.save(attempt);

            String gradingPrompt = PromptBuilder.buildSpeakingGradingPrompt(topic, attempt);
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

    private void parseAndSaveGradingResult(SpeakingAttempt attempt, String aiAnswer) {
        try {
            String jsonStr = JsonUtil.extractJson(aiAnswer);
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

    private Float getFloatField(JsonNode node, String field) {
        return JsonUtil.getFloatField(node, field);
    }
}
