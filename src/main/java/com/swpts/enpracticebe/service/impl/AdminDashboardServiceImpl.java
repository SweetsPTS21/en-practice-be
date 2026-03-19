package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.request.admin.RecentActivityRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.DashboardStatsResponse;
import com.swpts.enpracticebe.dto.response.admin.DashboardStatsResponse.ActivityStats;
import com.swpts.enpracticebe.dto.response.admin.DashboardStatsResponse.ContentCount;
import com.swpts.enpracticebe.dto.response.admin.DashboardStatsResponse.ContentStats;
import com.swpts.enpracticebe.dto.response.admin.RecentActivityResponse;
import com.swpts.enpracticebe.dto.response.admin.UserActivityChartResponse;
import com.swpts.enpracticebe.entity.DashboardDailyStat;
import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.entity.UserActivityLog;
import com.swpts.enpracticebe.mapper.admin.RecentActivityMapper;
import com.swpts.enpracticebe.repository.DashboardDailyStatRepository;
import com.swpts.enpracticebe.repository.UserActivityLogRepository;
import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.scheduler.DashboardStatsScheduler;
import com.swpts.enpracticebe.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final UserActivityLogRepository userActivityLogRepository;
    private final DashboardDailyStatRepository dashboardDailyStatRepository;
    private final DashboardStatsScheduler dashboardStatsScheduler;
    private final RecentActivityMapper recentActivityMapper;

    @Override
    @Cacheable(value = "dashboardStats")
    public DashboardStatsResponse getStats() {
        Optional<DashboardDailyStat> latestOpt = dashboardDailyStatRepository.findTopByOrderByStatDateDesc();
        if (latestOpt.isEmpty()) {
            // Fallback if no stats have been calculated yet
            return refreshStats();
        }

        return mapToResponse(latestOpt.get());
    }

    @Override
    public DashboardStatsResponse refreshStats() {
        DashboardDailyStat stat = dashboardStatsScheduler.calculateAndSaveStats(LocalDate.now());
        return mapToResponse(stat);
    }

    private DashboardStatsResponse mapToResponse(DashboardDailyStat stat) {
        return DashboardStatsResponse.builder()
                .totalUsers(stat.getTotalUsers())
                .activeUsersToday(stat.getActiveUsersToday())
                .newUsersThisWeek(stat.getNewUsersThisWeek())
                .contentStats(ContentStats.builder()
                        .ieltsTests(ContentCount.builder().total(stat.getTotalIelts()).published(stat.getPublishedIelts()).build())
                        .speakingTopics(ContentCount.builder().total(stat.getTotalSpeaking()).published(stat.getPublishedSpeaking()).build())
                        .writingTasks(ContentCount.builder().total(stat.getTotalWriting()).published(stat.getPublishedWriting()).build())
                        .build())
                .activityStats(ActivityStats.builder()
                        .totalAttempts(stat.getTotalAttempts())
                        .attemptsToday(stat.getAttemptsToday())
                        .vocabularyRecordsToday(stat.getVocabToday())
                        .build())
                .build();
    }

    @Override
    @Cacheable(value = "dashboardRecentActivities")
    public PageResponse<RecentActivityResponse> getRecentActivities(RecentActivityRequest request) {
        var logs = userActivityLogRepository.filterLogs(
                request.getUserName(),
                request.getActivityType(),
                request.getEntityName(),
                request.getFrom(),
                request.getTo(),
                PageRequest.of(request.getPage(), request.getSize()));

        Set<UUID> userIds = logs.getContent().stream().map(UserActivityLog::getUserId).collect(Collectors.toSet());
        Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return PageResponse.<RecentActivityResponse>builder()
                .items(recentActivityMapper.toDtoList(logs.getContent(), userMap))
                .page(logs.getNumber())
                .size(logs.getSize())
                .totalPages(logs.getTotalPages())
                .totalElements(logs.getTotalElements())
                .build();
    }

    @Override
    @Cacheable(value = "dashboardUserActivityChart", key = "#days")
    public List<UserActivityChartResponse> getUserActivityChart(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<DashboardDailyStat> stats = dashboardDailyStatRepository.findByStatDateBetweenOrderByStatDateAsc(startDate, endDate);
        Map<LocalDate, DashboardDailyStat> statMap = stats.stream()
                .collect(Collectors.toMap(DashboardDailyStat::getStatDate, s -> s));

        List<UserActivityChartResponse> chart = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            DashboardDailyStat dailyStat = statMap.get(date);

            if (dailyStat != null) {
                chart.add(UserActivityChartResponse.builder()
                        .date(date.format(formatter))
                        .activeUsers((int) dailyStat.getActiveUsersToday())
                        .attempts(dailyStat.getAttemptsToday())
                        .vocabularyRecords(dailyStat.getVocabToday())
                        .build());
            } else {
                // Return 0 if data for this date was not computed by cron job and stats were not manually refreshed
                chart.add(UserActivityChartResponse.builder()
                        .date(date.format(formatter))
                        .activeUsers(0)
                        .attempts(0L)
                        .vocabularyRecords(0L)
                        .build());
            }
        }

        return chart;
    }
}
