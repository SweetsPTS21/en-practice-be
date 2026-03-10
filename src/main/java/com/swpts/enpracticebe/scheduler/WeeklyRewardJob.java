package com.swpts.enpracticebe.scheduler;

import com.swpts.enpracticebe.constant.LeaderboardPeriod;
import com.swpts.enpracticebe.constant.LeaderboardScope;
import com.swpts.enpracticebe.constant.XpSource;
import com.swpts.enpracticebe.entity.LeaderboardSnapshot;
import com.swpts.enpracticebe.repository.LeaderboardSnapshotRepository;
import com.swpts.enpracticebe.service.XpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyRewardJob {

    private final LeaderboardSnapshotRepository snapshotRepository;
    private final XpService xpService;

    // Run every Monday at 00:05 UTC
    @Scheduled(cron = "0 5 0 * * MON")
    public void distributeWeeklyRewards() {
        log.info("Starting WeeklyRewardJob to distribute XP rewards for last week...");

        // Calculate periodKey for LAST week
        LocalDate lastWeek = LocalDate.now(ZoneOffset.UTC).minusWeeks(1);
        String periodKey = lastWeek.getYear() + "-W" + String.format("%02d", lastWeek.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));

        int page = 0;
        int size = 100;

        while (true) {
            Page<LeaderboardSnapshot> snapshotPage = snapshotRepository.findByPeriodTypeAndPeriodKeyAndScopeOrderByRankAsc(
                    LeaderboardPeriod.WEEKLY.name(), periodKey, LeaderboardScope.GLOBAL.name(), PageRequest.of(page, size));

            List<LeaderboardSnapshot> snapshots = snapshotPage.getContent();
            if (snapshots.isEmpty()) {
                break;
            }

            for (LeaderboardSnapshot snapshot : snapshots) {
                int rank = snapshot.getRank();
                int xpAmount = 0;

                if (rank == 1) {
                    xpAmount = 200;
                } else if (rank >= 2 && rank <= 3) {
                    xpAmount = 100;
                } else if (rank >= 4 && rank <= 10) {
                    xpAmount = 50;
                } else if (rank >= 11 && rank <= 50) {
                    xpAmount = 25;
                } else if (snapshot.getXp() > 0) {
                    xpAmount = 10;
                }

                if (xpAmount > 0) {
                    xpService.earnXp(snapshot.getUserId(), XpSource.WEEKLY_REWARD, "reward-" + periodKey, xpAmount);
                }
            }

            if (snapshotPage.isLast()) {
                break;
            }
            page++;
        }

        log.info("WeeklyRewardJob completed successfully.");
    }
}
