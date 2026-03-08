package com.swpts.enpracticebe.mapper;

import com.swpts.enpracticebe.dto.response.admin.AdminWritingTaskResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingSubmissionResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingTaskListResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingTaskResponse;
import com.swpts.enpracticebe.entity.WritingSubmission;
import com.swpts.enpracticebe.entity.WritingTask;
import org.springframework.stereotype.Component;

@Component
public class WritingMapper {
    public WritingTaskListResponse toListResponse(WritingTask task) {
        return WritingTaskListResponse.builder()
                .id(task.getId())
                .taskType(task.getTaskType().name())
                .title(task.getTitle())
                .difficulty(task.getDifficulty().name())
                .timeLimitMinutes(task.getTimeLimitMinutes())
                .minWords(task.getMinWords())
                .maxWords(task.getMaxWords())
                .createdAt(task.getCreatedAt())
                .build();
    }

    public WritingTaskResponse toTaskResponse(WritingTask task) {
        return WritingTaskResponse.builder()
                .id(task.getId())
                .taskType(task.getTaskType().name())
                .title(task.getTitle())
                .content(task.getContent())
                .instruction(task.getInstruction())
                .imageUrls(task.getImageUrls())
                .difficulty(task.getDifficulty().name())
                .isPublished(task.getIsPublished())
                .timeLimitMinutes(task.getTimeLimitMinutes())
                .minWords(task.getMinWords())
                .maxWords(task.getMaxWords())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public WritingSubmissionResponse toSubmissionResponse(WritingSubmission submission, WritingTask task) {
        return WritingSubmissionResponse.builder()
                .id(submission.getId())
                .taskId(submission.getTaskId())
                .taskTitle(task != null ? task.getTitle() : "Unknown")
                .taskType(task != null ? task.getTaskType().name() : null)
                .essayContent(submission.getEssayContent())
                .wordCount(submission.getWordCount())
                .timeSpentSeconds(submission.getTimeSpentSeconds())
                .status(submission.getStatus().name())
                .taskResponseScore(submission.getTaskResponseScore())
                .coherenceScore(submission.getCoherenceScore())
                .lexicalResourceScore(submission.getLexicalResourceScore())
                .grammarScore(submission.getGrammarScore())
                .overallBandScore(submission.getOverallBandScore())
                .aiFeedback(submission.getAiFeedback())
                .submittedAt(submission.getSubmittedAt())
                .gradedAt(submission.getGradedAt())
                .build();
    }

    public AdminWritingTaskResponse toAdminResponse(WritingTask task) {
        return AdminWritingTaskResponse.builder()
                .id(task.getId())
                .taskType(task.getTaskType().name())
                .title(task.getTitle())
                .content(task.getContent())
                .instruction(task.getInstruction())
                .imageUrls(task.getImageUrls())
                .aiGradingPrompt(task.getAiGradingPrompt())
                .difficulty(task.getDifficulty().name())
                .isPublished(task.getIsPublished())
                .timeLimitMinutes(task.getTimeLimitMinutes())
                .minWords(task.getMinWords())
                .maxWords(task.getMaxWords())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
