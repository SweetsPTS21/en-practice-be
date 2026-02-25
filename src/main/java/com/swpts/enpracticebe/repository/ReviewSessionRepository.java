package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.ReviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReviewSessionRepository extends JpaRepository<ReviewSession, UUID> {

    /**
     * Get the second-latest review session (skip the newest one).
     * Used to compare with the session just created.
     */
    @Query(value = """
            SELECT * FROM review_sessions
            WHERE user_id = :userId
            ORDER BY reviewed_at DESC
            OFFSET 1 LIMIT 1
            """, nativeQuery = true)
    Optional<ReviewSession> findSecondLatest(@Param("userId") UUID userId);
}
