package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.entity.DashboardDailyStat;
import com.swpts.enpracticebe.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardStatsScheduler {

    private final DashboardDailyStatRepository dashboardDailyStatRepository;
    private final UserRepository userRepository;
    private final IeltsTestRepository ieltsTestRepository;
    private final SpeakingTopicRepository speakingTopicRepository;
    private final WritingTaskRepository writingTaskRepository;
    private final IeltsTestAttemptRepository ieltsTestAttemptRepository;
    private final SpeakingAttemptRepository speakingAttemptRepository;
    private final WritingSubmissionRepository writingSubmissionRepository;
    private final VocabularyRecordRepository vocabularyRecordRepository;

    @Scheduled(cron = "0 0 0 * * *") // Runs daily at midnight
    @Transactional
    public void calculateDailyStats() {
        log.info("Starting dashboard daily stats calculation...");
        calculateAndSaveStats(LocalDate.now());
        log.info("Dashboard daily stats calculation completed.");
    }

    @Transactional
    @CacheEvict(value = {"dashboardStats", "dashboardUserActivityChart"}, allEntries = true)
    public DashboardDailyStat calculateAndSaveStats(LocalDate date) {
        Instant startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant startOfWeek = date.with(java.time.DayOfWeek.MONDAY)
                .atStartOfDay(ZoneOffset.UTC).toInstant();

        long totalUsers = userRepository.count();
        long newUsersThisWeek = userRepository.countByCreatedAtAfter(startOfWeek);

        // Count distinct users who had any activity on the given date
        Set<UUID> activeUserIds = new HashSet<>();
        activeUserIds.addAll(ieltsTestAttemptRepository.findUserIdsByStartedAtBetween(startOfDay, endOfDay));
        activeUserIds.addAll(speakingAttemptRepository.findUserIdsBySubmittedAtBetween(startOfDay, endOfDay));
        activeUserIds.addAll(writingSubmissionRepository.findUserIdsBySubmittedAtBetween(startOfDay, endOfDay));

        long ieltsTotal = ieltsTestRepository.count();
        long ieltsPublished = ieltsTestRepository.countByIsPublishedTrue();
        long speakingTotal = speakingTopicRepository.count();
        long speakingPublished = speakingTopicRepository.countByIsPublishedTrue();
        long writingTotal = writingTaskRepository.count();
        long writingPublished = writingTaskRepository.countByIsPublishedTrue();

        long totalAttempts = ieltsTestAttemptRepository.count()
                + speakingAttemptRepository.count()
                + writingSubmissionRepository.count();
        long attemptsToday = ieltsTestAttemptRepository.countByStartedAtBetween(startOfDay, endOfDay)
                + speakingAttemptRepository.countBySubmittedAtBetween(startOfDay, endOfDay)
                + writingSubmissionRepository.countBySubmittedAtBetween(startOfDay, endOfDay);
        long vocabToday = vocabularyRecordRepository.countByTestedAtBetween(startOfDay, endOfDay);

        DashboardDailyStat stat = dashboardDailyStatRepository.findByStatDate(date)
                .orElse(DashboardDailyStat.builder().statDate(date).build());

        stat.setTotalUsers(totalUsers);
        stat.setActiveUsersToday(activeUserIds.size());
        stat.setNewUsersThisWeek(newUsersThisWeek);
        stat.setTotalIelts(ieltsTotal);
        stat.setPublishedIelts(ieltsPublished);
        stat.setTotalSpeaking(speakingTotal);
        stat.setPublishedSpeaking(speakingPublished);
        stat.setTotalWriting(writingTotal);
        stat.setPublishedWriting(writingPublished);
        stat.setTotalAttempts(totalAttempts);
        stat.setAttemptsToday(attemptsToday);
        stat.setVocabToday(vocabToday);

        return dashboardDailyStatRepository.save(stat);
    }
}
