package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.IeltsTestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface IeltsTestAttemptRepository extends JpaRepository<IeltsTestAttempt, UUID> {

    List<IeltsTestAttempt> findByUserIdOrderByStartedAtDesc(UUID userId);

    List<IeltsTestAttempt> findByUserIdAndTestId(UUID userId, UUID testId);

    // Dashboard queries
    List<IeltsTestAttempt> findByStartedAtAfter(Instant after);

    long countByStartedAtAfter(Instant after);

    List<IeltsTestAttempt> findByStartedAtBetween(Instant start, Instant end);

    @org.springframework.data.jpa.repository.Query("SELECT a.userId FROM IeltsTestAttempt a WHERE a.startedAt BETWEEN :start AND :end")
    List<UUID> findUserIdsByStartedAtBetween(@org.springframework.data.repository.query.Param("start") Instant start, @org.springframework.data.repository.query.Param("end") Instant end);

    long countByStartedAtBetween(Instant start, Instant end);

    long countByUserId(UUID userId);
}
