package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.request.CreateWritingTaskRequest;
import com.swpts.enpracticebe.dto.request.UpdateWritingTaskRequest;
import com.swpts.enpracticebe.dto.request.WritingTaskFilterRequest;
import com.swpts.enpracticebe.dto.response.AdminWritingTaskResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.entity.WritingTask;
import com.swpts.enpracticebe.repository.WritingTaskRepository;
import com.swpts.enpracticebe.service.AdminWritingService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminWritingServiceImpl implements AdminWritingService {

    private final WritingTaskRepository taskRepository;

    public AdminWritingServiceImpl(WritingTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // ─── List Tasks ─────────────────────────────────────────────────────────────

    @Override
    @Cacheable(value = "adminWritingTaskList", key = "#request.page + '-' + #request.size + '-' + #request.taskType + '-' + #request.difficulty + '-' + #request.isPublished")
    public PageResponse<AdminWritingTaskResponse> listTasks(WritingTaskFilterRequest request) {
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean hasTaskType = request.getTaskType() != null && !request.getTaskType().isBlank();
        boolean hasDifficulty = request.getDifficulty() != null && !request.getDifficulty().isBlank();
        boolean hasPublished = request.getIsPublished() != null;

        Page<WritingTask> page = findTasks(hasTaskType, hasDifficulty, hasPublished, request, pageable);

        List<AdminWritingTaskResponse> items = page.getContent().stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminWritingTaskResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(items)
                .build();
    }

    // ─── Get Task Detail ────────────────────────────────────────────────────────

    @Override
    @Cacheable(value = "writingTaskDetail", key = "#taskId")
    public AdminWritingTaskResponse getTaskDetail(UUID taskId) {
        WritingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Writing task not found: " + taskId));
        return toAdminResponse(task);
    }

    // ─── Create Task ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "adminWritingTaskList", allEntries = true),
            @CacheEvict(value = "writingTaskList", allEntries = true)
    })
    public AdminWritingTaskResponse createTask(CreateWritingTaskRequest request) {
        WritingTask task = WritingTask.builder()
                .taskType(WritingTask.TaskType.valueOf(request.getTaskType()))
                .title(request.getTitle())
                .content(request.getContent())
                .instruction(request.getInstruction())
                .imageUrls(request.getImageUrls())
                .aiGradingPrompt(request.getAiGradingPrompt())
                .difficulty(WritingTask.Difficulty.valueOf(request.getDifficulty()))
                .isPublished(request.getIsPublished())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .minWords(request.getMinWords())
                .maxWords(request.getMaxWords())
                .build();
        task = taskRepository.save(task);
        return toAdminResponse(task);
    }

    // ─── Update Task ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "writingTaskDetail", key = "#taskId"),
            @CacheEvict(value = "adminWritingTaskList", allEntries = true),
            @CacheEvict(value = "writingTaskList", allEntries = true)
    })
    public AdminWritingTaskResponse updateTask(UUID taskId, UpdateWritingTaskRequest request) {
        WritingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Writing task not found: " + taskId));

        task.setTaskType(WritingTask.TaskType.valueOf(request.getTaskType()));
        task.setTitle(request.getTitle());
        task.setContent(request.getContent());
        task.setInstruction(request.getInstruction());
        task.setImageUrls(request.getImageUrls());
        task.setAiGradingPrompt(request.getAiGradingPrompt());
        task.setDifficulty(WritingTask.Difficulty.valueOf(request.getDifficulty()));
        task.setIsPublished(request.getIsPublished());
        task.setTimeLimitMinutes(request.getTimeLimitMinutes());
        task.setMinWords(request.getMinWords());
        task.setMaxWords(request.getMaxWords());
        task = taskRepository.save(task);

        return toAdminResponse(task);
    }

    // ─── Delete Task ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "writingTaskDetail", key = "#taskId"),
            @CacheEvict(value = "adminWritingTaskList", allEntries = true),
            @CacheEvict(value = "writingTaskList", allEntries = true)
    })
    public void deleteTask(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Writing task not found: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    // ─── Toggle Publish ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "writingTaskDetail", key = "#taskId"),
            @CacheEvict(value = "adminWritingTaskList", allEntries = true),
            @CacheEvict(value = "writingTaskList", allEntries = true)
    })
    public void togglePublish(UUID taskId, boolean published) {
        WritingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Writing task not found: " + taskId));
        task.setIsPublished(published);
        taskRepository.save(task);
    }

    // ─── Private helpers ────────────────────────────────────────────────────────

    private Page<WritingTask> findTasks(boolean hasTaskType, boolean hasDifficulty, boolean hasPublished,
            WritingTaskFilterRequest request, PageRequest pageable) {
        WritingTask.TaskType taskType = hasTaskType ? WritingTask.TaskType.valueOf(request.getTaskType()) : null;
        WritingTask.Difficulty difficulty = hasDifficulty ? WritingTask.Difficulty.valueOf(request.getDifficulty())
                : null;

        if (hasTaskType && hasDifficulty && hasPublished) {
            return taskRepository.findByTaskTypeAndDifficultyAndIsPublished(taskType, difficulty,
                    request.getIsPublished(), pageable);
        } else if (hasTaskType && hasDifficulty) {
            return taskRepository.findByTaskTypeAndDifficulty(taskType, difficulty, pageable);
        } else if (hasTaskType && hasPublished) {
            return taskRepository.findByTaskTypeAndIsPublished(taskType, request.getIsPublished(), pageable);
        } else if (hasDifficulty && hasPublished) {
            return taskRepository.findByDifficultyAndIsPublished(difficulty, request.getIsPublished(), pageable);
        } else if (hasTaskType) {
            return taskRepository.findByTaskType(taskType, pageable);
        } else if (hasDifficulty) {
            return taskRepository.findByDifficulty(difficulty, pageable);
        } else if (hasPublished) {
            return taskRepository.findByIsPublished(request.getIsPublished(), pageable);
        } else {
            return taskRepository.findAll(pageable);
        }
    }

    private AdminWritingTaskResponse toAdminResponse(WritingTask task) {
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
