package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.WritingTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WritingTaskRepository extends JpaRepository<WritingTask, UUID> {

    // Admin filters
    Page<WritingTask> findByTaskType(WritingTask.TaskType taskType, Pageable pageable);

    Page<WritingTask> findByDifficulty(WritingTask.Difficulty difficulty, Pageable pageable);

    Page<WritingTask> findByIsPublished(Boolean isPublished, Pageable pageable);

    Page<WritingTask> findByTaskTypeAndDifficulty(WritingTask.TaskType taskType, WritingTask.Difficulty difficulty,
            Pageable pageable);

    Page<WritingTask> findByTaskTypeAndIsPublished(WritingTask.TaskType taskType, Boolean isPublished,
            Pageable pageable);

    Page<WritingTask> findByDifficultyAndIsPublished(WritingTask.Difficulty difficulty, Boolean isPublished,
            Pageable pageable);

    Page<WritingTask> findByTaskTypeAndDifficultyAndIsPublished(WritingTask.TaskType taskType,
            WritingTask.Difficulty difficulty, Boolean isPublished, Pageable pageable);

    // User-facing (published only)
    Page<WritingTask> findByIsPublishedTrue(Pageable pageable);

    Page<WritingTask> findByTaskTypeAndIsPublishedTrue(WritingTask.TaskType taskType, Pageable pageable);

    Page<WritingTask> findByDifficultyAndIsPublishedTrue(WritingTask.Difficulty difficulty, Pageable pageable);

    Page<WritingTask> findByTaskTypeAndDifficultyAndIsPublishedTrue(WritingTask.TaskType taskType,
            WritingTask.Difficulty difficulty, Pageable pageable);
}
