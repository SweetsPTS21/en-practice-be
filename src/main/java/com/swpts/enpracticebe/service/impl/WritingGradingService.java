package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.response.ai.AiAskResponse;
import com.swpts.enpracticebe.entity.WritingSubmission;
import com.swpts.enpracticebe.entity.WritingTask;
import com.swpts.enpracticebe.repository.WritingSubmissionRepository;
import com.swpts.enpracticebe.repository.WritingTaskRepository;
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
 * Separate bean so @Async is properly proxied by Spring AOP.
 * Self-invocation within the same class bypasses the proxy.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WritingGradingService {

    private final WritingSubmissionRepository submissionRepository;
    private final WritingTaskRepository taskRepository;
    private final OpenClawService openClawService;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper;

    @Async("aiGradingExecutor")
    public void gradeEssayAsync(UUID submissionId, UUID userId) {
        try {
            WritingSubmission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));

            WritingTask task = taskRepository.findById(submission.getTaskId())
                    .orElseThrow(() -> new RuntimeException("Writing task not found: " + submission.getTaskId()));

            // Update status to GRADING
            submission.setStatus(WritingSubmission.SubmissionStatus.GRADING);
            submissionRepository.save(submission);

            // Build AI grading prompt
            String gradingPrompt = PromptBuilder.buildWritingGradingPrompt(task, submission);

            // Call AI
            AiAskResponse aiResponse = openClawService.askAi(gradingPrompt, userId);

            // Parse AI response
            parseAndSaveGradingResult(submission, aiResponse.getAnswer());

            // Send push notification
            try {
                pushNotificationService.sendNotificationToUser(
                        userId,
                        "Writing Graded",
                        "Your IELTS Writing submission has been graded. Band: " + submission.getOverallBandScore());
            } catch (Exception e) {
                log.warn("Failed to send push notification to user {}: {}", userId, e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error grading essay submission {}: {}", submissionId, e.getMessage(), e);
            try {
                WritingSubmission submission = submissionRepository.findById(submissionId).orElse(null);
                if (submission != null) {
                    submission.setStatus(WritingSubmission.SubmissionStatus.FAILED);
                    submission.setAiFeedback("AI grading failed: " + e.getMessage());
                    submissionRepository.save(submission);
                }
            } catch (Exception ex) {
                log.error("Failed to update submission status to FAILED: {}", ex.getMessage());
            }
        }
    }

    private void parseAndSaveGradingResult(WritingSubmission submission, String aiAnswer) {
        try {
            String jsonStr = JsonUtil.extractJson(aiAnswer);
            JsonNode node = objectMapper.readTree(jsonStr);

            submission.setTaskResponseScore(getFloatField(node, "task_response"));
            submission.setCoherenceScore(getFloatField(node, "coherence"));
            submission.setLexicalResourceScore(getFloatField(node, "lexical_resource"));
            submission.setGrammarScore(getFloatField(node, "grammar"));
            submission.setOverallBandScore(getFloatField(node, "overall_band"));

            String feedback = node.has("feedback") ? node.get("feedback").asText() : aiAnswer;
            submission.setAiFeedback(feedback);
            submission.setStatus(WritingSubmission.SubmissionStatus.GRADED);
            submission.setGradedAt(Instant.now());

        } catch (Exception e) {
            log.warn("Failed to parse AI grading JSON, saving raw response: {}", e.getMessage());
            submission.setAiFeedback(aiAnswer);
            submission.setStatus(WritingSubmission.SubmissionStatus.GRADED);
            submission.setGradedAt(Instant.now());
        }

        submissionRepository.save(submission);
    }

    private Float getFloatField(JsonNode node, String field) {
        return JsonUtil.getFloatField(node, field);
    }
}
