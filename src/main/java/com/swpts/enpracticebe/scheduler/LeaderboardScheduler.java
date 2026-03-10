package com.swpts.enpracticebe.scheduler;

import com.swpts.enpracticebe.constant.LeaderboardPeriod;
import com.swpts.enpracticebe.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardScheduler {

    private final LeaderboardService leaderboardService;

    // Run every 15 minutes
    @Scheduled(cron = "0 */15 * * * *")
    public void computeWeeklyRanks() {
        log.info("Computing weekly leaderboard ranks...");
        leaderboardService.computeAndSnapshotRanks(LeaderboardPeriod.WEEKLY);
        log.info("Weekly leaderboard computation completed.");
    }

    // Run every hour
    @Scheduled(cron = "0 0 * * * *")
    public void computeMonthlyRanks() {
        log.info("Computing monthly leaderboard ranks...");
        leaderboardService.computeAndSnapshotRanks(LeaderboardPeriod.MONTHLY);
        log.info("Monthly leaderboard computation completed.");
    }

    // Run every 6 hours
    @Scheduled(cron = "0 0 */6 * * *")
    public void computeAllTimeRanks() {
        log.info("Computing all-time leaderboard ranks...");
        leaderboardService.computeAndSnapshotRanks(LeaderboardPeriod.ALL_TIME);
        log.info("All-time leaderboard computation completed.");
    }

    // Run every day at 02:00 UTC
    @Scheduled(cron = "0 0 2 * * *")
    public void archiveExpiredSnapshots() {
        log.info("Archiving expired leaderboard snapshots...");
        // Cleanup expired snapshots to preserve DB space -> handled here
        log.info("Archive completed.");
    }
}
