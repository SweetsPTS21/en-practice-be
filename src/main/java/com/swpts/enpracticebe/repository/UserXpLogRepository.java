package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.UserXpLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface UserXpLogRepository extends JpaRepository<UserXpLog, UUID> {
    
    Page<UserXpLog> findByUserIdOrderByEarnedAtDesc(UUID userId, Pageable pageable);
    
    // For anti-gaming checks
    boolean existsByUserIdAndSourceAndSourceId(UUID userId, String source, String sourceId);

    @Query("SELECT COALESCE(SUM(x.xpAmount), 0) FROM UserXpLog x WHERE x.userId = :userId AND x.earnedAt BETWEEN :start AND :end")
    Integer sumXpByUserIdAndEarnedAtBetween(@Param("userId") UUID userId,
                                            @Param("start") Instant start,
                                            @Param("end") Instant end);
}
