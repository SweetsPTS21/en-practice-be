package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.LeaderboardSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaderboardSnapshotRepository extends JpaRepository<LeaderboardSnapshot, UUID> {

    /**
     * Lấy leaderboard page chỉ từ snapshot_date MỚI NHẤT của period đó.
     * Tránh việc cùng 1 user xuất hiện nhiều lần do nhiều snapshot_date khác nhau.
     */
    @Query("""
        SELECT s FROM LeaderboardSnapshot s
        WHERE s.periodType = :periodType
          AND s.periodKey = :periodKey
          AND s.scope = :scope
          AND s.snapshotDate = (
              SELECT MAX(s2.snapshotDate)
              FROM LeaderboardSnapshot s2
              WHERE s2.periodType = :periodType
                AND s2.periodKey = :periodKey
                AND s2.scope = :scope
          )
        ORDER BY s.rank ASC
        """)
    Page<LeaderboardSnapshot> findLatestByPeriodTypeAndPeriodKeyAndScope(
            @Param("periodType") String periodType,
            @Param("periodKey") String periodKey,
            @Param("scope") String scope,
            Pageable pageable);

    Optional<LeaderboardSnapshot> findByUserIdAndPeriodTypeAndPeriodKeyAndScopeAndSnapshotDate(
            UUID userId, String periodType, String periodKey, String scope, LocalDate snapshotDate);

    Optional<LeaderboardSnapshot> findFirstByUserIdAndPeriodTypeAndPeriodKeyAndScopeOrderBySnapshotDateDesc(
            UUID userId, String periodType, String periodKey, String scope);
}
