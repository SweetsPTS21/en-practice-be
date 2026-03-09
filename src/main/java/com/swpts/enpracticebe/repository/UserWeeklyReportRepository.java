package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.UserWeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserWeeklyReportRepository extends JpaRepository<UserWeeklyReport, UUID> {
    List<UserWeeklyReport> findByUserIdOrderByWeekEndDesc(UUID userId);
}
