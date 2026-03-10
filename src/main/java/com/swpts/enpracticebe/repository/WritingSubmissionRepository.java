package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.WritingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WritingSubmissionRepository extends JpaRepository<WritingSubmission, UUID> {

    List<WritingSubmission> findByUserIdOrderBySubmittedAtDesc(UUID userId);

    List<WritingSubmission> findTop10ByUserIdAndStatusOrderByGradedAtDesc(UUID userId, WritingSubmission.SubmissionStatus status);

    List<WritingSubmission> findByTaskIdAndUserIdOrderBySubmittedAtDesc(UUID taskId, UUID userId);

    // Dashboard queries
    List<WritingSubmission> findByUserIdAndSubmittedAtBetween(UUID userId, Instant start, Instant end);

    @Query("SELECT COALESCE(SUM(a.timeSpentSeconds), 0) FROM WritingSubmission a WHERE a.userId = :userId AND a.submittedAt BETWEEN :start AND :end")
    Integer sumTimeSpentByUserIdAndDateRange(@Param("userId") UUID userId, @Param("start") Instant start, @Param("end") Instant end);

    boolean existsByUserIdAndSubmittedAtBetween(UUID userId, Instant start, Instant end);

    Optional<WritingSubmission> findFirstByUserIdAndStatusOrderByGradedAtDesc(UUID userId, WritingSubmission.SubmissionStatus status);
    List<WritingSubmission> findBySubmittedAtAfter(Instant after);

    long countBySubmittedAtAfter(Instant after);

    List<WritingSubmission> findBySubmittedAtBetween(Instant start, Instant end);

    @Query("SELECT a.userId FROM WritingSubmission a WHERE a.submittedAt BETWEEN :start AND :end")
    List<UUID> findUserIdsBySubmittedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    long countBySubmittedAtBetween(Instant start, Instant end);

    long countByUserId(UUID userId);
}
