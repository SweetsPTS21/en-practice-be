package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.UserXpLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserXpLogRepository extends JpaRepository<UserXpLog, UUID> {
    
    Page<UserXpLog> findByUserIdOrderByEarnedAtDesc(UUID userId, Pageable pageable);
    
    // For anti-gaming checks
    boolean existsByUserIdAndSourceAndSourceId(UUID userId, String source, String sourceId);
}
