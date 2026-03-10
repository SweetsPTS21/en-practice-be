package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.LeaderboardSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaderboardSnapshotRepository extends JpaRepository<LeaderboardSnapshot, UUID> {

    Page<LeaderboardSnapshot> findByPeriodTypeAndPeriodKeyAndScopeOrderByRankAsc(
            String periodType, String periodKey, String scope, Pageable pageable);

    Optional<LeaderboardSnapshot> findByUserIdAndPeriodTypeAndPeriodKeyAndScopeAndSnapshotDate(
            UUID userId, String periodType, String periodKey, String scope, LocalDate snapshotDate);

    Optional<LeaderboardSnapshot> findFirstByUserIdAndPeriodTypeAndPeriodKeyAndScopeOrderBySnapshotDateDesc(
            UUID userId, String periodType, String periodKey, String scope);
}
