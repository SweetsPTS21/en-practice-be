package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.WritingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WritingSubmissionRepository extends JpaRepository<WritingSubmission, UUID> {

    List<WritingSubmission> findByUserIdOrderBySubmittedAtDesc(UUID userId);

    List<WritingSubmission> findByTaskIdAndUserIdOrderBySubmittedAtDesc(UUID taskId, UUID userId);

    // Dashboard queries
    List<WritingSubmission> findByUserIdAndSubmittedAtBetween(UUID userId, Instant start, Instant end);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.timeSpentSeconds), 0) FROM WritingSubmission a WHERE a.userId = :userId AND a.submittedAt BETWEEN :start AND :end")
    Integer sumTimeSpentByUserIdAndDateRange(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("start") Instant start, @org.springframework.data.repository.query.Param("end") Instant end);

    boolean existsByUserIdAndSubmittedAtBetween(UUID userId, Instant start, Instant end);

    Optional<WritingSubmission> findFirstByUserIdAndStatusOrderByGradedAtDesc(UUID userId, WritingSubmission.SubmissionStatus status);
    List<WritingSubmission> findBySubmittedAtAfter(Instant after);

    long countBySubmittedAtAfter(Instant after);

    List<WritingSubmission> findBySubmittedAtBetween(Instant start, Instant end);

    @org.springframework.data.jpa.repository.Query("SELECT a.userId FROM WritingSubmission a WHERE a.submittedAt BETWEEN :start AND :end")
    List<UUID> findUserIdsBySubmittedAtBetween(@org.springframework.data.repository.query.Param("start") Instant start, @org.springframework.data.repository.query.Param("end") Instant end);

    long countBySubmittedAtBetween(Instant start, Instant end);

    long countByUserId(UUID userId);
}
