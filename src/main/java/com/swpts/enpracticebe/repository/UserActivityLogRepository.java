package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, UUID> {

    List<UserActivityLog> findTop20ByOrderByCreatedAtDesc();

    @Query("""
        SELECT l FROM UserActivityLog l
        LEFT JOIN User u ON l.userId = u.id
        WHERE (:userName IS NULL OR u.displayName LIKE %:userName%)
          AND (:activityType IS NULL OR l.activityType = :activityType)
          AND (:entityName IS NULL OR l.entityName LIKE %:entityName%)
          AND l.createdAt >= COALESCE(:from, l.createdAt)
          AND l.createdAt <= COALESCE(:to, l.createdAt)
        ORDER BY l.createdAt DESC
    """)
    Page<UserActivityLog> filterLogs(String userName, String activityType, String entityName, Instant from, Instant to, Pageable pageable);

    Page<UserActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
