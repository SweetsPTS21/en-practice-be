package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.DashboardDailyStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DashboardDailyStatRepository extends JpaRepository<DashboardDailyStat, UUID> {

    Optional<DashboardDailyStat> findByStatDate(LocalDate statDate);

    Optional<DashboardDailyStat> findTopByOrderByStatDateDesc();
}
