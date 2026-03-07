package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.request.SubmitWritingRequest;
import com.swpts.enpracticebe.dto.request.WritingTaskFilterRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.WritingSubmissionResponse;
import com.swpts.enpracticebe.dto.response.WritingTaskListResponse;
import com.swpts.enpracticebe.dto.response.WritingTaskResponse;
import com.swpts.enpracticebe.entity.WritingSubmission;
import com.swpts.enpracticebe.entity.WritingTask;
import com.swpts.enpracticebe.repository.WritingSubmissionRepository;
import com.swpts.enpracticebe.repository.WritingTaskRepository;
import com.swpts.enpracticebe.service.WritingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
public class WritingServiceImpl implements WritingService {

    private final WritingTaskRepository taskRepository;
    private final WritingSubmissionRepository submissionRepository;
    private final WritingGradingService gradingService;

    public WritingServiceImpl(WritingTaskRepository taskRepository,
            WritingSubmissionRepository submissionRepository,
            WritingGradingService gradingService) {
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.gradingService = gradingService;
    }

    // ─── List Writing Tasks (published only) ────────────────────────────────────

    @Override
    public PageResponse<WritingTaskListResponse> getWritingTasks(WritingTaskFilterRequest request) {
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean hasTaskType = request.getTaskType() != null && !request.getTaskType().isBlank();
        boolean hasDifficulty = request.getDifficulty() != null && !request.getDifficulty().isBlank();

        Page<WritingTask> page;

        if (hasTaskType && hasDifficulty) {
            page = taskRepository.findByTaskTypeAndDifficultyAndIsPublishedTrue(
                    WritingTask.TaskType.valueOf(request.getTaskType()),
                    WritingTask.Difficulty.valueOf(request.getDifficulty()),
                    pageable);
        } else if (hasTaskType) {
            page = taskRepository.findByTaskTypeAndIsPublishedTrue(
                    WritingTask.TaskType.valueOf(request.getTaskType()), pageable);
        } else if (hasDifficulty) {
            page = taskRepository.findByDifficultyAndIsPublishedTrue(
                    WritingTask.Difficulty.valueOf(request.getDifficulty()), pageable);
        } else {
            page = taskRepository.findByIsPublishedTrue(pageable);
        }

        List<WritingTaskListResponse> items = page.getContent().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());

        return PageResponse.<WritingTaskListResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(items)
                .build();
    }

    // ─── Get Writing Task Detail ────────────────────────────────────────────────

    @Override
    public WritingTaskResponse getWritingTaskDetail(UUID taskId) {
        WritingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Writing task not found: " + taskId));
        return toTaskResponse(task);
    }

    // ─── Submit Essay ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WritingSubmissionResponse submitEssay(UUID taskId, SubmitWritingRequest request) {
        UUID userId = getCurrentUserId();

        WritingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Writing task not found: " + taskId));

        int wordCount = countWords(request.getEssayContent());

        WritingSubmission submission = WritingSubmission.builder()
                .userId(userId)
                .taskId(taskId)
                .essayContent(request.getEssayContent())
                .wordCount(wordCount)
                .timeSpentSeconds(request.getTimeSpentSeconds())
                .status(WritingSubmission.SubmissionStatus.SUBMITTED)
                .build();
        submission = submissionRepository.save(submission);

        // Delegate to separate bean AFTER transaction commits,
        // so the submission is visible in DB when the async thread reads it
        final UUID submissionId = submission.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                gradingService.gradeEssayAsync(submissionId, userId);
            }
        });

        return toSubmissionResponse(submission, task);
    }

    // ─── Get Submission Detail ──────────────────────────────────────────────────

    @Override
    public WritingSubmissionResponse getSubmissionDetail(UUID submissionId) {
        UUID userId = getCurrentUserId();

        WritingSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));

        if (!submission.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: this submission does not belong to you");
        }

        WritingTask task = taskRepository.findById(submission.getTaskId())
                .orElseThrow(() -> new RuntimeException("Writing task not found: " + submission.getTaskId()));

        return toSubmissionResponse(submission, task);
    }

    // ─── Submission History ─────────────────────────────────────────────────────

    @Override
    public PageResponse<WritingSubmissionResponse> getSubmissionHistory(int page, int size) {
        UUID userId = getCurrentUserId();
        List<WritingSubmission> allSubmissions = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);

        // Manual pagination
        int total = allSubmissions.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<WritingSubmission> pageSubmissions = allSubmissions.subList(fromIndex, toIndex);

        // Build task map for titles
        List<UUID> taskIds = pageSubmissions.stream()
                .map(WritingSubmission::getTaskId)
                .distinct()
                .collect(Collectors.toList());
        java.util.Map<UUID, WritingTask> taskMap = taskRepository.findAllById(taskIds).stream()
                .collect(Collectors.toMap(WritingTask::getId, t -> t));

        List<WritingSubmissionResponse> items = pageSubmissions.stream()
                .map(sub -> {
                    WritingTask task = taskMap.get(sub.getTaskId());
                    return toSubmissionResponse(sub, task);
                })
                .collect(Collectors.toList());

        return PageResponse.<WritingSubmissionResponse>builder()
                .page(page)
                .size(size)
                .totalElements((long) total)
                .totalPages((int) Math.ceil((double) total / size))
                .items(items)
                .build();
    }

    // ─── Private helpers ────────────────────────────────────────────────────────

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private int countWords(String text) {
        if (text == null || text.isBlank())
            return 0;
        return text.trim().split("\\s+").length;
    }

    private WritingTaskListResponse toListResponse(WritingTask task) {
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

    private WritingTaskResponse toTaskResponse(WritingTask task) {
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

    private WritingSubmissionResponse toSubmissionResponse(WritingSubmission submission, WritingTask task) {
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
}
