package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.response.AiAskResponse;
import com.swpts.enpracticebe.entity.WritingSubmission;
import com.swpts.enpracticebe.entity.WritingTask;
import com.swpts.enpracticebe.repository.WritingSubmissionRepository;
import com.swpts.enpracticebe.repository.WritingTaskRepository;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.service.PushNotificationService;
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
public class WritingGradingService {

    private final WritingSubmissionRepository submissionRepository;
    private final WritingTaskRepository taskRepository;
    private final OpenClawService openClawService;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper;

    public WritingGradingService(WritingSubmissionRepository submissionRepository,
            WritingTaskRepository taskRepository,
            OpenClawService openClawService,
            PushNotificationService pushNotificationService,
            ObjectMapper objectMapper) {
        this.submissionRepository = submissionRepository;
        this.taskRepository = taskRepository;
        this.openClawService = openClawService;
        this.pushNotificationService = pushNotificationService;
        this.objectMapper = objectMapper;
    }

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
            String gradingPrompt = buildGradingPrompt(task, submission);

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

    // ─── Private helpers ────────────────────────────────────────────────────────

    private String buildGradingPrompt(WritingTask task, WritingSubmission submission) {
        String customPrompt = task.getAiGradingPrompt();
        if (customPrompt != null && !customPrompt.isBlank()) {
            return customPrompt
                    .replace("{essay}", submission.getEssayContent())
                    .replace("{task_content}", task.getContent())
                    .replace("{task_type}", task.getTaskType().name());
        }

        String taskTypeDesc = task.getTaskType() == WritingTask.TaskType.TASK_1
                ? "IELTS Writing Task 1 (describe a chart/graph/process/map)"
                : "IELTS Writing Task 2 (essay)";

        return String.format("""
                You are an IELTS Writing examiner. Grade the following %s submission.

                **Task/Question:**
                %s

                **Student's Essay (%d words):**
                %s

                Grade on these 4 criteria (each 0.0 to 9.0, in 0.5 increments):
                1. Task Response (TR)
                2. Coherence and Cohesion (CC)
                3. Lexical Resource (LR)
                4. Grammatical Range and Accuracy (GRA)

                You MUST respond in the following JSON format only, no extra text:
                {
                  "task_response": 6.5,
                  "coherence": 6.0,
                  "lexical_resource": 6.5,
                  "grammar": 6.0,
                  "overall_band": 6.5,
                  "feedback": "Your detailed feedback in markdown format here..."
                }
                """,
                taskTypeDesc,
                task.getContent(),
                submission.getWordCount(),
                submission.getEssayContent());
    }

    private void parseAndSaveGradingResult(WritingSubmission submission, String aiAnswer) {
        try {
            String jsonStr = extractJson(aiAnswer);
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

    private String extractJson(String text) {
        if (text.contains("```json")) {
            int start = text.indexOf("```json") + 7;
            int end = text.indexOf("```", start);
            if (end > start) {
                return text.substring(start, end).trim();
            }
        }
        if (text.contains("```")) {
            int start = text.indexOf("```") + 3;
            int end = text.indexOf("```", start);
            if (end > start) {
                return text.substring(start, end).trim();
            }
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
