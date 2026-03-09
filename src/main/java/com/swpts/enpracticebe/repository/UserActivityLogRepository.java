package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, UUID> {

    List<UserActivityLog> findTop20ByOrderByCreatedAtDesc();

    Page<UserActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
