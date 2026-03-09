package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.SpeakingAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpeakingAttemptRepository extends JpaRepository<SpeakingAttempt, UUID> {

    List<SpeakingAttempt> findByUserIdOrderBySubmittedAtDesc(UUID userId);

    List<SpeakingAttempt> findByTopicIdAndUserIdOrderBySubmittedAtDesc(UUID topicId, UUID userId);

    // Dashboard queries
    List<SpeakingAttempt> findBySubmittedAtAfter(Instant after);

    long countBySubmittedAtAfter(Instant after);

    List<SpeakingAttempt> findBySubmittedAtBetween(Instant start, Instant end);

    @org.springframework.data.jpa.repository.Query("SELECT a.userId FROM SpeakingAttempt a WHERE a.submittedAt BETWEEN :start AND :end")
    List<UUID> findUserIdsBySubmittedAtBetween(@org.springframework.data.repository.query.Param("start") Instant start, @org.springframework.data.repository.query.Param("end") Instant end);

    long countBySubmittedAtBetween(Instant start, Instant end);

    long countByUserId(UUID userId);
}
