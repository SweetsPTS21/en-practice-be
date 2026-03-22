package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.response.dashboard.DayStatus;
import com.swpts.enpracticebe.dto.response.profile.LevelInfoResponse;
import com.swpts.enpracticebe.entity.IeltsTestAttempt;
import com.swpts.enpracticebe.entity.SpeakingAttempt;
import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.entity.UserProfileSnapshot;
import com.swpts.enpracticebe.entity.WritingSubmission;
import com.swpts.enpracticebe.repository.IeltsTestAttemptRepository;
import com.swpts.enpracticebe.repository.SpeakingAttemptRepository;
import com.swpts.enpracticebe.repository.UserDictionaryRepository;
import com.swpts.enpracticebe.repository.UserProfileSnapshotRepository;
import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.repository.UserXpLogRepository;
import com.swpts.enpracticebe.repository.VocabularyRecordRepository;
import com.swpts.enpracticebe.repository.WritingSubmissionRepository;
import com.swpts.enpracticebe.service.LevelService;
import com.swpts.enpracticebe.service.UserProfileSnapshotService;
import com.swpts.enpracticebe.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileSnapshotServiceImpl implements UserProfileSnapshotService {

    private static final int HEATMAP_DAYS = 30;
    private static final int APPROX_VOCAB_SECONDS_PER_RECORD = 15;

    private final UserRepository userRepository;
    private final UserProfileSnapshotRepository userProfileSnapshotRepository;
    private final UserXpLogRepository userXpLogRepository;
    private final UserDictionaryRepository userDictionaryRepository;
    private final VocabularyRecordRepository vocabularyRecordRepository;
    private final IeltsTestAttemptRepository ieltsTestAttemptRepository;
    private final SpeakingAttemptRepository speakingAttemptRepository;
    private final WritingSubmissionRepository writingSubmissionRepository;
    private final LevelService levelService;

    @Override
    public UserProfileSnapshot getOrComputeSnapshot(UUID userId) {
        return userProfileSnapshotRepository.findById(userId)
                .orElseGet(() -> computeSnapshot(userId));
    }

    @Override
    @Transactional
    public UserProfileSnapshot computeSnapshot(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LevelInfoResponse levelInfo = levelService.calculateLevelInfo(user.getTotalXp());
        StreakComputation streak = calculateStreak(userId);
        int weeklyXp = calculateWeeklyXp(userId);
        long totalIeltsAttempts = ieltsTestAttemptRepository.countByUserId(userId);
        long totalSpeakingAttempts = speakingAttemptRepository.countByUserId(userId);
        long totalWritingSubmissions = writingSubmissionRepository.countByUserId(userId);
        long totalLessonsCompleted = totalIeltsAttempts + totalSpeakingAttempts + totalWritingSubmissions;
        long totalWordsLearned = vocabularyRecordRepository.countAllUniqueWords(userId);
        long vocabReviewingWords = vocabularyRecordRepository.countUniqueWrongWords(userId);
        long vocabMasteredWords = Math.max(0, totalWordsLearned - vocabReviewingWords);
        int totalStudyMinutes = calculateTotalStudyMinutes(userId);
        int studiedMinutesToday = calculateStudiedMinutesToday(userId);
        long wordsToReviewToday = userDictionaryRepository.countDueForReview(userId, Instant.now());
        long favoriteWords = userDictionaryRepository.countByUserIdAndIsFavoriteTrue(userId);
        long newWords = userDictionaryRepository.countByUserIdAndProficiencyLevel(userId, 0);
        long learningWords = Math.max(0L, totalWordsLearned - newWords - vocabMasteredWords);
        BandComputation band = calculateBandProgress(userId);

        UserProfileSnapshot snapshot = userProfileSnapshotRepository.findById(userId)
                .orElseGet(() -> UserProfileSnapshot.builder().userId(userId).build());

        snapshot.setTotalXp(levelInfo.getTotalXp());
        snapshot.setCurrentLevel(levelInfo.getCurrentLevel());
        snapshot.setCurrentLevelMinXp(levelInfo.getCurrentLevelMinXp());
        snapshot.setNextLevel(levelInfo.getNextLevel());
        snapshot.setNextLevelMinXp(levelInfo.getNextLevelMinXp());
        snapshot.setXpIntoCurrentLevel(levelInfo.getXpIntoCurrentLevel());
        snapshot.setXpNeededForNextLevel(levelInfo.getXpNeededForNextLevel());
        snapshot.setLevelProgressPercentage(levelInfo.getProgressPercentage());
        snapshot.setWeeklyXp(weeklyXp);
        snapshot.setTotalLessonsCompleted(totalLessonsCompleted);
        snapshot.setTotalWordsLearned(totalWordsLearned);
        snapshot.setTotalStudyMinutes(totalStudyMinutes);
        snapshot.setStudiedMinutesToday(studiedMinutesToday);
        snapshot.setWordsToReviewToday(wordsToReviewToday);
        snapshot.setFavoriteWords(favoriteWords);
        snapshot.setNewWords(newWords);
        snapshot.setLearningWords(learningWords);
        snapshot.setCurrentStreak(streak.currentStreak());
        snapshot.setLongestStreak(streak.longestStreak());
        snapshot.setActiveDaysLast30(streak.activeDaysLast30());
        snapshot.setLast30DaysHeatmap(streak.heatmap());
        snapshot.setListeningBand(band.listeningBand());
        snapshot.setReadingBand(band.readingBand());
        snapshot.setSpeakingBand(band.speakingBand());
        snapshot.setWritingBand(band.writingBand());
        snapshot.setOverallBand(band.overallBand());
        snapshot.setVocabTotalWords(totalWordsLearned);
        snapshot.setVocabMasteredWords(vocabMasteredWords);
        snapshot.setVocabReviewingWords(vocabReviewingWords);
        snapshot.setComputedAt(Instant.now());

        return userProfileSnapshotRepository.save(snapshot);
    }

    @Override
    public void refreshAllActiveUserSnapshots() {
        List<UUID> userIds = userRepository.findAllActiveUserIds();
        int success = 0;
        int failed = 0;

        for (UUID userId : userIds) {
            try {
                computeSnapshot(userId);
                success++;
            } catch (Exception ex) {
                failed++;
                log.warn("Failed to compute profile snapshot for user {}: {}", userId, ex.getMessage());
            }
        }

        log.info("User profile snapshot refresh completed. Success: {}, Failed: {}", success, failed);
    }

    private int calculateWeeklyXp(UUID userId) {
        Instant startOfWeek = LocalDate.now(ZoneOffset.UTC)
                .with(java.time.DayOfWeek.MONDAY)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        return safeInteger(userXpLogRepository.sumXpByUserIdAndEarnedAtBetween(userId, startOfWeek, Instant.now()));
    }

    private int calculateTotalStudyMinutes(UUID userId) {
        int totalSeconds = safeInteger(ieltsTestAttemptRepository.sumTimeSpentByUserId(userId))
                + safeInteger(speakingAttemptRepository.sumTimeSpentByUserId(userId))
                + safeInteger(writingSubmissionRepository.sumTimeSpentByUserId(userId));

        totalSeconds += (int) vocabularyRecordRepository.countByUserId(userId) * APPROX_VOCAB_SECONDS_PER_RECORD;
        return totalSeconds / 60;
    }

    private int calculateStudiedMinutesToday(UUID userId) {
        Instant todayStart = DateUtil.getStartOfToday();
        Instant now = Instant.now();

        int studiedSeconds = safeInteger(ieltsTestAttemptRepository.sumTimeSpentByUserIdAndDateRange(userId, todayStart, now))
                + safeInteger(speakingAttemptRepository.sumTimeSpentByUserIdAndDateRange(userId, todayStart, now))
                + safeInteger(writingSubmissionRepository.sumTimeSpentByUserIdAndDateRange(userId, todayStart, now));

        studiedSeconds += (int) vocabularyRecordRepository.countByUserIdAndTestedAtAfter(userId, todayStart)
                * APPROX_VOCAB_SECONDS_PER_RECORD;

        return studiedSeconds / 60;
    }

    private StreakComputation calculateStreak(UUID userId) {
        Set<LocalDate> activityDates = collectActivityDates(userId);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        int currentStreak = 0;
        LocalDate cursor = today;
        while (activityDates.contains(cursor)) {
            currentStreak++;
            cursor = cursor.minusDays(1);
        }

        if (currentStreak == 0 && activityDates.contains(today.minusDays(1))) {
            cursor = today.minusDays(1);
            while (activityDates.contains(cursor)) {
                currentStreak++;
                cursor = cursor.minusDays(1);
            }
        }

        int longestStreak = 0;
        List<LocalDate> sortedDates = activityDates.stream().sorted().toList();
        LocalDate previous = null;
        int currentRun = 0;
        for (LocalDate date : sortedDates) {
            if (previous == null || previous.plusDays(1).equals(date)) {
                currentRun++;
            } else {
                currentRun = 1;
            }
            longestStreak = Math.max(longestStreak, currentRun);
            previous = date;
        }

        List<DayStatus> heatmap = new ArrayList<>();
        int activeDaysLast30 = 0;
        for (int i = HEATMAP_DAYS - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            boolean hasActivity = activityDates.contains(date);
            if (hasActivity) {
                activeDaysLast30++;
            }
            heatmap.add(new DayStatus(date, hasActivity));
        }

        return new StreakComputation(currentStreak, longestStreak, activeDaysLast30, heatmap);
    }

    private Set<LocalDate> collectActivityDates(UUID userId) {
        Set<LocalDate> activityDates = new HashSet<>();

        for (java.sql.Date sqlDate : vocabularyRecordRepository.findDistinctRecordDates(userId)) {
            activityDates.add(sqlDate.toLocalDate());
        }
        for (IeltsTestAttempt attempt : ieltsTestAttemptRepository.findByUserIdOrderByStartedAtDesc(userId)) {
            activityDates.add(LocalDate.ofInstant(attempt.getStartedAt(), ZoneOffset.UTC));
        }
        for (SpeakingAttempt attempt : speakingAttemptRepository.findByUserIdOrderBySubmittedAtDesc(userId)) {
            activityDates.add(LocalDate.ofInstant(attempt.getSubmittedAt(), ZoneOffset.UTC));
        }
        for (WritingSubmission submission : writingSubmissionRepository.findByUserIdOrderBySubmittedAtDesc(userId)) {
            activityDates.add(LocalDate.ofInstant(submission.getSubmittedAt(), ZoneOffset.UTC));
        }

        return activityDates;
    }

    private BandComputation calculateBandProgress(UUID userId) {
        Float listeningBand = 0f;
        Float readingBand = 0f;
        Optional<IeltsTestAttempt> latestIelts = ieltsTestAttemptRepository.findFirstByUserIdAndStatusOrderByCompletedAtDesc(
                userId, IeltsTestAttempt.AttemptStatus.COMPLETED);
        if (latestIelts.isPresent() && latestIelts.get().getBandScore() != null) {
            listeningBand = latestIelts.get().getBandScore();
            readingBand = latestIelts.get().getBandScore();
        }

        Float speakingBand = 0f;
        Optional<SpeakingAttempt> latestSpeaking = speakingAttemptRepository.findFirstByUserIdAndStatusOrderByGradedAtDesc(
                userId, SpeakingAttempt.AttemptStatus.GRADED);
        if (latestSpeaking.isPresent() && latestSpeaking.get().getOverallBandScore() != null) {
            speakingBand = latestSpeaking.get().getOverallBandScore();
        }

        Float writingBand = 0f;
        Optional<WritingSubmission> latestWriting = writingSubmissionRepository.findFirstByUserIdAndStatusOrderByGradedAtDesc(
                userId, WritingSubmission.SubmissionStatus.GRADED);
        if (latestWriting.isPresent() && latestWriting.get().getOverallBandScore() != null) {
            writingBand = latestWriting.get().getOverallBandScore();
        }

        int count = 0;
        float total = 0f;
        if (listeningBand > 0) {
            total += listeningBand;
            count++;
        }
        if (speakingBand > 0) {
            total += speakingBand;
            count++;
        }
        if (writingBand > 0) {
            total += writingBand;
            count++;
        }
        Float overallBand = count > 0 ? (float) (Math.round((total / count) * 2) / 2.0) : 0f;

        return new BandComputation(listeningBand, readingBand, speakingBand, writingBand, overallBand);
    }

    private int safeInteger(Integer value) {
        return value != null ? value : 0;
    }

    private record StreakComputation(int currentStreak,
                                     int longestStreak,
                                     int activeDaysLast30,
                                     List<DayStatus> heatmap) {
    }

    private record BandComputation(Float listeningBand,
                                   Float readingBand,
                                   Float speakingBand,
                                   Float writingBand,
                                   Float overallBand) {
    }
}
