package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.SpeakingAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpeakingAttemptRepository extends JpaRepository<SpeakingAttempt, UUID> {

    List<SpeakingAttempt> findByUserIdOrderBySubmittedAtDesc(UUID userId);

    List<SpeakingAttempt> findTop5ByUserIdOrderBySubmittedAtDesc(UUID userId);

    List<SpeakingAttempt> findTop10ByUserIdAndStatusOrderByGradedAtDesc(UUID userId, SpeakingAttempt.AttemptStatus status);

    List<SpeakingAttempt> findByTopicIdAndUserIdOrderBySubmittedAtDesc(UUID topicId, UUID userId);

    // Dashboard queries
    List<SpeakingAttempt> findByUserIdAndSubmittedAtBetween(UUID userId, Instant start, Instant end);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.timeSpentSeconds), 0) FROM SpeakingAttempt a WHERE a.userId = :userId AND a.submittedAt BETWEEN :start AND :end")
    Integer sumTimeSpentByUserIdAndDateRange(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("start") Instant start, @org.springframework.data.repository.query.Param("end") Instant end);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.timeSpentSeconds), 0) FROM SpeakingAttempt a WHERE a.userId = :userId")
    Integer sumTimeSpentByUserId(@org.springframework.data.repository.query.Param("userId") UUID userId);

    boolean existsByUserIdAndSubmittedAtBetween(UUID userId, Instant start, Instant end);

    Optional<SpeakingAttempt> findFirstByUserIdAndStatusOrderByGradedAtDesc(UUID userId, SpeakingAttempt.AttemptStatus status);
    List<SpeakingAttempt> findBySubmittedAtAfter(Instant after);

    long countBySubmittedAtAfter(Instant after);

    List<SpeakingAttempt> findBySubmittedAtBetween(Instant start, Instant end);

    @org.springframework.data.jpa.repository.Query("SELECT a.userId FROM SpeakingAttempt a WHERE a.submittedAt BETWEEN :start AND :end")
    List<UUID> findUserIdsBySubmittedAtBetween(@org.springframework.data.repository.query.Param("start") Instant start, @org.springframework.data.repository.query.Param("end") Instant end);

    long countBySubmittedAtBetween(Instant start, Instant end);

    long countByUserId(UUID userId);
}
