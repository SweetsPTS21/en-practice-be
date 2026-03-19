package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.constant.ActivityType;
import com.swpts.enpracticebe.dto.request.writing.SubmitWritingRequest;
import com.swpts.enpracticebe.dto.request.writing.WritingTaskFilterRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingSubmissionResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingTaskListResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingTaskResponse;
import com.swpts.enpracticebe.entity.WritingSubmission;
import com.swpts.enpracticebe.entity.WritingTask;
import com.swpts.enpracticebe.exception.ForbiddenException;
import com.swpts.enpracticebe.mapper.WritingMapper;
import com.swpts.enpracticebe.repository.WritingSubmissionRepository;
import com.swpts.enpracticebe.repository.WritingTaskRepository;
import com.swpts.enpracticebe.service.WritingService;
import com.swpts.enpracticebe.service.UserActivityLogService;
import com.swpts.enpracticebe.constant.XpSource;
import com.swpts.enpracticebe.service.XpService;
import com.swpts.enpracticebe.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WritingServiceImpl implements WritingService {

    private final WritingTaskRepository taskRepository;
    private final WritingSubmissionRepository submissionRepository;
    private final WritingGradingService gradingService;
    private final WritingMapper writingMapper;
    private final AuthUtil authUtil;
    private final UserActivityLogService userActivityLogService;
    private final XpService xpService;

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
                .map(writingMapper::toListResponse)
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
        return writingMapper.toTaskResponse(task);
    }

    // ─── Submit Essay ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WritingSubmissionResponse submitEssay(UUID taskId, SubmitWritingRequest request) {
        UUID userId = authUtil.getUserId();

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

        userActivityLogService.logActivity(userId, ActivityType.WRITING_SUBMISSION, submission.getId(), task.getTitle());

        // Delegate to separate bean AFTER transaction commits,
        // so the submission is visible in DB when the async thread reads it
        final UUID submissionId = submission.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                gradingService.gradeEssayAsync(submissionId, userId);
            }
        });

        xpService.earnXp(userId, XpSource.WRITING_SUBMISSION, submissionId.toString(), 15);

        return writingMapper.toSubmissionResponse(submission, task);
    }

    // ─── Get Submission Detail ──────────────────────────────────────────────────

    @Override
    public WritingSubmissionResponse getSubmissionDetail(UUID submissionId) {
        UUID userId = authUtil.getUserId();

        WritingSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        if (!submission.getUserId().equals(userId) && !authUtil.isAdmin()) {
            throw new ForbiddenException("Unauthorized: this submission does not belong to you");
        }

        WritingTask task = taskRepository.findById(submission.getTaskId())
                .orElseThrow(() -> new NoSuchElementException("Writing task not found: " + submission.getTaskId()));

        return writingMapper.toSubmissionResponse(submission, task);
    }

    // ─── Submission History ─────────────────────────────────────────────────────

    @Override
    public PageResponse<WritingSubmissionResponse> getSubmissionHistory(int page, int size) {
        UUID userId = authUtil.getUserId();
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
                    return writingMapper.toSubmissionResponse(sub, task);
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

    private int countWords(String text) {
        if (text == null || text.isBlank())
            return 0;
        return text.trim().split("\\s+").length;
    }
}
