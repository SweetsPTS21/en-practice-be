package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.constant.LeaderboardPeriod;
import com.swpts.enpracticebe.constant.LeaderboardScope;
import com.swpts.enpracticebe.constant.RankChangeDirection;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.leaderboard.LeaderboardEntry;
import com.swpts.enpracticebe.dto.response.leaderboard.LeaderboardResponse;
import com.swpts.enpracticebe.dto.response.leaderboard.LeaderboardSummaryResponse;
import com.swpts.enpracticebe.dto.response.leaderboard.MyRankInfo;
import com.swpts.enpracticebe.entity.LeaderboardSnapshot;
import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.repository.LeaderboardSnapshotRepository;
import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    @Cacheable(value = "leaderboardPage", key = "#period + ':' + #scope + ':' + #targetBand + ':' + #page + ':' + #size")
    @Override
    public LeaderboardResponse getLeaderboard(LeaderboardPeriod period, LeaderboardScope scope, Float targetBand, int page, int size) {
        String periodKey = getPeriodKey(period);
        Page<LeaderboardSnapshot> snapshotPage = snapshotRepository.findLatestByPeriodTypeAndPeriodKeyAndScope(
                period.name(), periodKey, scope.name(), PageRequest.of(page, size));

        List<LeaderboardEntry> entries = snapshotPage.getContent().stream().map(s -> {
            User user = userRepository.findById(s.getUserId()).orElse(new User());
            return LeaderboardEntry.builder()
                    .rank(s.getRank())
                    .userId(user.getId())
                    .displayName(user.getDisplayName())
                    .avatarUrl(null)
                    .targetBand(null)
                    .xp(s.getXp())
                    .currentStreak(0)
                    .rankChange(s.getPreviousRank() != null ? s.getPreviousRank() - s.getRank() : 0)
                    .rankChangeDirection(getDirection(s.getPreviousRank(), s.getRank()))
                    .build();
        }).collect(Collectors.toList());

        return LeaderboardResponse.builder()
                .topUsers(entries)
                .page(PageResponse.builder()
                        .page(page)
                        .totalPages(snapshotPage.getTotalPages())
                        .totalElements(snapshotPage.getTotalElements())
                        .build())
                .build();
    }

    @Cacheable(value = "leaderboardSummary", key = "#userId + ':' + #period")
    @Override
    public LeaderboardSummaryResponse getLeaderboardSummary(UUID userId, LeaderboardPeriod period) {
        String periodKey = getPeriodKey(period);
        
        // Fetch top 3
        Page<LeaderboardSnapshot> top3Page = snapshotRepository.findLatestByPeriodTypeAndPeriodKeyAndScope(
                period.name(), periodKey, LeaderboardScope.GLOBAL.name(), PageRequest.of(0, 3));
        
        List<LeaderboardEntry> topThree = top3Page.getContent().stream().map(s -> {
            User user = userRepository.findById(s.getUserId()).orElse(new User());
            return LeaderboardEntry.builder()
                    .rank(s.getRank())
                    .displayName(user.getDisplayName())
                    .xp(s.getXp())
                    .build();
        }).collect(Collectors.toList());

        // Fetch My Rank
        LeaderboardSnapshot mySnapshot = snapshotRepository.findFirstByUserIdAndPeriodTypeAndPeriodKeyAndScopeOrderBySnapshotDateDesc(
                userId, period.name(), periodKey, LeaderboardScope.GLOBAL.name()).orElse(null);

        MyRankInfo myRankInfo = null;
        if (mySnapshot != null) {
            myRankInfo = MyRankInfo.builder()
                    .rank(mySnapshot.getRank())
                    .xp(mySnapshot.getXp())
                    .rankChange(mySnapshot.getPreviousRank() != null ? mySnapshot.getPreviousRank() - mySnapshot.getRank() : 0)
                    .rankChangeDirection(getDirection(mySnapshot.getPreviousRank(), mySnapshot.getRank()))
                    .build();
        }

        return LeaderboardSummaryResponse.builder()
                .period(period)
                .topThree(topThree)
                .myRank(myRankInfo)
                .build();
    }

    @CacheEvict(value = "leaderboardPage", allEntries = true)
    @Override
    public void computeAndSnapshotRanks(LeaderboardPeriod period) {
        String periodKey = getPeriodKey(period);
        String startDate;
        String endDate;
        LocalDate now = LocalDate.now(ZoneOffset.UTC);

        if (period == LeaderboardPeriod.WEEKLY) {
            LocalDate startOfWeek = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            LocalDate endOfWeek = startOfWeek.plusDays(7);
            startDate = startOfWeek.toString();
            endDate = endOfWeek.toString();
        } else if (period == LeaderboardPeriod.MONTHLY) {
            LocalDate startOfMonth = now.withDayOfMonth(1);
            LocalDate endOfMonth = startOfMonth.plusMonths(1);
            startDate = startOfMonth.toString();
            endDate = endOfMonth.toString();
        } else {
            startDate = "1970-01-01";
            endDate = "2099-12-31";
        }

        String sql = """
            WITH xp_agg AS (
                SELECT user_id, SUM(xp_amount) AS total_xp
                FROM user_xp_logs
                WHERE earned_at >= ?::timestamp AND earned_at < ?::timestamp
                GROUP BY user_id
            ),
            ranked AS (
                SELECT
                    user_id,
                    total_xp,
                    RANK() OVER (ORDER BY total_xp DESC) AS new_rank
                FROM xp_agg
            )
            INSERT INTO leaderboard_snapshots (id, user_id, period_type, period_key, scope, xp, rank, previous_rank, snapshot_date)
            SELECT
                gen_random_uuid(),
                r.user_id,
                ?,
                ?, 
                'GLOBAL',
                r.total_xp,
                r.new_rank,
                ls.rank,           
                CURRENT_DATE
            FROM ranked r
            LEFT JOIN leaderboard_snapshots ls
                ON ls.user_id = r.user_id
                AND ls.period_type = ?
                AND ls.period_key = ?
                AND ls.scope = 'GLOBAL'
                AND ls.snapshot_date = (
                    SELECT MAX(snapshot_date)
                    FROM leaderboard_snapshots
                    WHERE user_id = r.user_id
                      AND period_type = ?
                      AND period_key = ?
                      AND scope = 'GLOBAL'
                )
            ON CONFLICT (user_id, period_type, period_key, scope, snapshot_date)
            DO UPDATE SET
                xp = EXCLUDED.xp,
                previous_rank = leaderboard_snapshots.rank,
                rank = EXCLUDED.rank;
            """;

        jdbcTemplate.update(sql, startDate, endDate, period.name(), periodKey, period.name(), periodKey, period.name(), periodKey);
    }

    private String getPeriodKey(LeaderboardPeriod period) {
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        if (period == LeaderboardPeriod.WEEKLY) {
            return now.getYear() + "-W" + String.format("%02d", now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
        } else if (period == LeaderboardPeriod.MONTHLY) {
            return now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        return "ALL";
    }

    private RankChangeDirection getDirection(Integer prev, int current) {
        if (prev == null) return RankChangeDirection.NEW;
        if (current < prev) return RankChangeDirection.UP;
        if (current > prev) return RankChangeDirection.DOWN;
        return RankChangeDirection.STABLE;
    }
}
