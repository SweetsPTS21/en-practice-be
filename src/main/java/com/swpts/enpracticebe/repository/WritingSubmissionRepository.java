package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.WritingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WritingSubmissionRepository extends JpaRepository<WritingSubmission, UUID> {

    List<WritingSubmission> findByUserIdOrderBySubmittedAtDesc(UUID userId);

    List<WritingSubmission> findByTaskIdAndUserIdOrderBySubmittedAtDesc(UUID taskId, UUID userId);
}
