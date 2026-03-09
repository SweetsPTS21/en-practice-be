package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.response.admin.DashboardStatsResponse;
import com.swpts.enpracticebe.dto.response.admin.DashboardStatsResponse.*;
import com.swpts.enpracticebe.dto.response.admin.RecentActivityResponse;
import com.swpts.enpracticebe.dto.response.admin.UserActivityChartResponse;
import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.entity.*;
import com.swpts.enpracticebe.repository.*;
import com.swpts.enpracticebe.service.AdminDashboardService;
import com.swpts.enpracticebe.service.DashboardStatsScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final IeltsTestAttemptRepository ieltsTestAttemptRepository;
    private final SpeakingAttemptRepository speakingAttemptRepository;
    private final WritingSubmissionRepository writingSubmissionRepository;
    private final VocabularyRecordRepository vocabularyRecordRepository;
    private final DashboardDailyStatRepository dashboardDailyStatRepository;
    private final DashboardStatsScheduler dashboardStatsScheduler;

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
    public List<RecentActivityResponse> getRecentActivities() {
        List<RecentActivityResponse> activities = new ArrayList<>();

        // Fetch last 20 from each source, then merge and take top 20
        PageRequest top20 = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "startedAt"));
        ieltsTestAttemptRepository.findAll(top20).forEach(attempt -> {
            User user = userRepository.findById(attempt.getUserId()).orElse(null);
            activities.add(RecentActivityResponse.builder()
                    .userId(attempt.getUserId())
                    .userName(user != null ? user.getDisplayName() : "Unknown")
                    .activityType("IELTS_ATTEMPT")
                    .entityName("IELTS Test Attempt")
                    .createdAt(attempt.getStartedAt())
                    .build());
        });

        speakingAttemptRepository.findAll(
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "submittedAt"))
        ).forEach(attempt -> {
            User user = userRepository.findById(attempt.getUserId()).orElse(null);
            activities.add(RecentActivityResponse.builder()
                    .userId(attempt.getUserId())
                    .userName(user != null ? user.getDisplayName() : "Unknown")
                    .activityType("SPEAKING_ATTEMPT")
                    .entityName("Speaking Attempt")
                    .createdAt(attempt.getSubmittedAt())
                    .build());
        });

        writingSubmissionRepository.findAll(
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "submittedAt"))
        ).forEach(sub -> {
            User user = userRepository.findById(sub.getUserId()).orElse(null);
            activities.add(RecentActivityResponse.builder()
                    .userId(sub.getUserId())
                    .userName(user != null ? user.getDisplayName() : "Unknown")
                    .activityType("WRITING_SUBMISSION")
                    .entityName("Writing Submission")
                    .createdAt(sub.getSubmittedAt())
                    .build());
        });

        // Sort by createdAt DESC and take top 20
        activities.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        return activities.stream().limit(20).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "dashboardUserActivityChart", key = "#days")
    public List<UserActivityChartResponse> getUserActivityChart(int days) {
        List<UserActivityChartResponse> chart = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Instant dayStart = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant dayEnd = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

            // Count distinct active users on this day
            Set<UUID> activeUserIds = new HashSet<>();
            ieltsTestAttemptRepository.findByStartedAtBetween(dayStart, dayEnd)
                    .forEach(a -> activeUserIds.add(a.getUserId()));
            speakingAttemptRepository.findBySubmittedAtBetween(dayStart, dayEnd)
                    .forEach(a -> activeUserIds.add(a.getUserId()));
            writingSubmissionRepository.findBySubmittedAtBetween(dayStart, dayEnd)
                    .forEach(a -> activeUserIds.add(a.getUserId()));

            long attempts = ieltsTestAttemptRepository.countByStartedAtBetween(dayStart, dayEnd)
                    + speakingAttemptRepository.countBySubmittedAtBetween(dayStart, dayEnd)
                    + writingSubmissionRepository.countBySubmittedAtBetween(dayStart, dayEnd);

            long vocabRecords = vocabularyRecordRepository.countByTestedAtBetween(dayStart, dayEnd);

            chart.add(UserActivityChartResponse.builder()
                    .date(date.format(formatter))
                    .activeUsers(activeUserIds.size())
                    .attempts(attempts)
                    .vocabularyRecords(vocabRecords)
                    .build());
        }

        return chart;
    }
}
