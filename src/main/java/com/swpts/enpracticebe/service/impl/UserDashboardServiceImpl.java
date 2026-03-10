package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.constant.LeaderboardPeriod;
import com.swpts.enpracticebe.constant.XpSource;
import com.swpts.enpracticebe.dto.response.dashboard.*;
import com.swpts.enpracticebe.dto.response.leaderboard.LeaderboardSummaryResponse;
import com.swpts.enpracticebe.entity.*;
import com.swpts.enpracticebe.repository.*;
import com.swpts.enpracticebe.service.LeaderboardService;
import com.swpts.enpracticebe.service.UserDashboardService;
import com.swpts.enpracticebe.service.XpService;
import com.swpts.enpracticebe.util.AuthUtil;
import com.swpts.enpracticebe.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class UserDashboardServiceImpl implements UserDashboardService {

    private final AuthUtil authUtil;
    private final VocabularyRecordRepository vocabularyRecordRepository;
    private final IeltsTestAttemptRepository ieltsTestAttemptRepository;
    private final SpeakingAttemptRepository speakingAttemptRepository;
    private final WritingSubmissionRepository writingSubmissionRepository;
    private final LeaderboardService leaderboardService;
    private final XpService xpService;
    private final UserPracticeRecommendationRepository recommendationRepository;

    @Override
    public DashboardResponse getDashboard() {
        UUID userId = authUtil.getUserId();
        Instant todayStart = DateUtil.getStartOfToday();
        Instant now = Instant.now();

        CompletableFuture<StreakInfo> streakFuture = CompletableFuture.supplyAsync(() -> calculateStreak(userId));
        CompletableFuture<TodayGoal> goalFuture = CompletableFuture.supplyAsync(() -> calculateTodayGoal(userId, todayStart, now));
        CompletableFuture<List<DailyTask>> tasksFuture = CompletableFuture.supplyAsync(() -> generateDailyTasks(userId, todayStart, now));
        CompletableFuture<ProgressOverview> progressFuture = CompletableFuture.supplyAsync(() -> getProgressOverview(userId));
        CompletableFuture<List<RecentActivity>> recentFuture = CompletableFuture.supplyAsync(() -> getRecentActivities(userId));
        CompletableFuture<List<QuickPracticeItem>> quickFuture = CompletableFuture.supplyAsync(this::generateQuickPractice);
        CompletableFuture<LeaderboardSummaryResponse> leaderboardFuture = CompletableFuture.supplyAsync(() -> leaderboardService.getLeaderboardSummary(userId, LeaderboardPeriod.WEEKLY));

        // Read pre-computed recommendations from DB (populated by RecommendationScheduler every 6h)
        CompletableFuture<Optional<UserPracticeRecommendation>> recFuture = CompletableFuture.supplyAsync(() -> recommendationRepository.findByUserId(userId));

        CompletableFuture.allOf(streakFuture, goalFuture, tasksFuture, progressFuture, recentFuture,
                quickFuture, leaderboardFuture, recFuture).join();

        // Map pre-computed data or use defaults
        Optional<UserPracticeRecommendation> precomputed = recFuture.join();

        List<String> weakSkills;
        List<RecommendedPractice> recommendedPractice;

        if (precomputed.isPresent()) {
            UserPracticeRecommendation rec = precomputed.get();
            weakSkills = rec.getWeakSkills() != null ? rec.getWeakSkills() : List.of();
            recommendedPractice = rec.getRecommendations() != null
                    ? rec.getRecommendations().stream().map(item -> RecommendedPractice.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .type(item.getType())
                    .difficulty(item.getDifficulty())
                    .estimatedTime(item.getEstimatedTime())
                    .path(item.getPath())
                    .reason(item.getReason())
                    .priority(item.getPriority())
                    .build()).toList()
                    : List.of();
        } else {
            // No pre-computed data yet — return quick defaults, scheduler will populate later
            weakSkills = List.of("Academic Vocabulary", "Map Labeling");
            recommendedPractice = List.of(
                    RecommendedPractice.builder()
                            .id("r1")
                            .title("Diagnostic Test")
                            .description("Take a short Listening test to assess your baseline.")
                            .type("LISTENING").difficulty("Medium").estimatedTime("15 mins")
                            .path("/ielts").reason("We need a baseline to personalize your learning path.").priority(1)
                            .build()
            );
        }

        // MVP Smart Reminder: if current streak is 0 but longest streak > 0, or just haven't studied today
        StreakInfo streak = streakFuture.join();
        SmartReminder smartReminder = null;
        if (streak.getCurrentStreak() == 0) {
            smartReminder = SmartReminder.builder()
                    .title("Keep your streak alive!")
                    .message("You haven't practiced today. 5 minutes is all it takes.")
                    .type("WARNING")
                    .ctaText("Practice Now")
                    .ctaPath("/ielts")
                    .build();
        }

        return DashboardResponse.builder()
                .streak(streak)
                .todayGoal(goalFuture.join())
                .dailyTasks(tasksFuture.join())
                .progress(progressFuture.join())
                .recentActivities(recentFuture.join())
                .quickPractice(quickFuture.join())
                .weakSkills(weakSkills)
                .recommendedPractice(recommendedPractice)
                .smartReminder(smartReminder)
                .leaderboardSummary(leaderboardFuture.join())
                .build();
    }


    private StreakInfo calculateStreak(UUID userId) {
        // Simple streak for MVP: Use dates from Vocabulary records as baseline
        List<java.sql.Date> vocabDates = vocabularyRecordRepository.findDistinctRecordDates(userId);

        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        Set<LocalDate> activityDates = new HashSet<>();
        for (java.sql.Date sqlDate : vocabDates) {
            activityDates.add(sqlDate.toLocalDate());
        }

        // Also fetch from Ielts/Speaking/Writing for the last 7 days to merge with activityDates
        Instant sevenDaysAgo = today.minusDays(6).atStartOfDay(ZoneId.of("UTC")).toInstant();
        List<IeltsTestAttempt> ielts = ieltsTestAttemptRepository.findByUserIdAndStartedAtBetween(userId, sevenDaysAgo, Instant.now());
        for (IeltsTestAttempt a : ielts) activityDates.add(LocalDate.ofInstant(a.getStartedAt(), ZoneId.of("UTC")));

        List<SpeakingAttempt> speakings = speakingAttemptRepository.findByUserIdAndSubmittedAtBetween(userId, sevenDaysAgo, Instant.now());
        for (SpeakingAttempt a : speakings)
            activityDates.add(LocalDate.ofInstant(a.getSubmittedAt(), ZoneId.of("UTC")));

        List<WritingSubmission> writings = writingSubmissionRepository.findByUserIdAndSubmittedAtBetween(userId, sevenDaysAgo, Instant.now());
        for (WritingSubmission a : writings)
            activityDates.add(LocalDate.ofInstant(a.getSubmittedAt(), ZoneId.of("UTC")));

        // Calculate current streak
        int currentStreak = 0;
        int longestStreak = 0; // Not fully tracked in MVP yet, approximate to current
        LocalDate dateCursor = today;

        while (activityDates.contains(dateCursor)) {
            currentStreak++;
            dateCursor = dateCursor.minusDays(1);
        }

        if (currentStreak == 0 && activityDates.contains(today.minusDays(1))) {
            // User hasnt studied today, but studied yesterday
            dateCursor = today.minusDays(1);
            while (activityDates.contains(dateCursor)) {
                currentStreak++;
                dateCursor = dateCursor.minusDays(1);
            }
        }
        if (currentStreak > longestStreak) longestStreak = currentStreak;

        // Build 7-day heatmap (Mon-Sun style, but we use last 7 days ending today)
        List<DayStatus> heatmap = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            heatmap.add(new DayStatus(d, activityDates.contains(d)));
        }

        return StreakInfo.builder()
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .weeklyHeatmap(heatmap)
                .build();
    }

    private TodayGoal calculateTodayGoal(UUID userId, Instant start, Instant end) {
        int targetMinutes = 30; // Default MVP
        int studiedSeconds = 0;

        studiedSeconds += ieltsTestAttemptRepository.sumTimeSpentByUserIdAndDateRange(userId, start, end);
        studiedSeconds += speakingAttemptRepository.sumTimeSpentByUserIdAndDateRange(userId, start, end);
        studiedSeconds += writingSubmissionRepository.sumTimeSpentByUserIdAndDateRange(userId, start, end);

        // Vocab time (approx 15 seconds per word)
        long vocabCount = vocabularyRecordRepository.countByUserIdAndTestedAtAfter(userId, start);
        studiedSeconds += (int) (vocabCount * 15);

        int studiedMinutes = studiedSeconds / 60;
        int percentage = Math.min(100, (int) ((double) studiedMinutes / targetMinutes * 100));

        return TodayGoal.builder()
                .targetMinutes(targetMinutes)
                .studiedMinutes(studiedMinutes)
                .percentage(percentage)
                .build();
    }

    private List<DailyTask> generateDailyTasks(UUID userId, Instant start, Instant end) {
        List<DailyTask> tasks = new ArrayList<>();

        // 1. Vocabulary Review
        long vocabCount = vocabularyRecordRepository.countByUserIdAndTestedAtAfter(userId, start);
        boolean vocabCompleted = vocabCount >= 20;
        if (vocabCompleted) xpService.earnXp(userId, XpSource.DAILY_TASK_COMPLETE, "t1-" + start.toString(), 10);
        tasks.add(DailyTask.builder()
                .id("t1")
                .title("Review 20 vocabulary")
                .description("Learn or review 20 words")
                .completed(vocabCompleted)
                .type("VOCAB")
                .build());

        // 2. Listening Practice
        boolean hasIelts = ieltsTestAttemptRepository.existsByUserIdAndStartedAtBetween(userId, start, end);
        if (hasIelts) xpService.earnXp(userId, XpSource.DAILY_TASK_COMPLETE, "t2-" + start.toString(), 10);
        tasks.add(DailyTask.builder()
                .id("t2")
                .title("Listening mini test")
                .description("Complete at least 1 reading or listening test")
                .completed(hasIelts)
                .type("LISTENING")
                .build());

        // 3. Speaking Practice
        boolean hasSpeaking = speakingAttemptRepository.existsByUserIdAndSubmittedAtBetween(userId, start, end);
        if (hasSpeaking) xpService.earnXp(userId, XpSource.DAILY_TASK_COMPLETE, "t3-" + start.toString(), 10);
        tasks.add(DailyTask.builder()
                .id("t3")
                .title("Speaking question")
                .description("Practice speaking for 2 minutes")
                .completed(hasSpeaking)
                .type("SPEAKING")
                .build());

        // 4. Writing Practice
        boolean hasWriting = writingSubmissionRepository.existsByUserIdAndSubmittedAtBetween(userId, start, end);
        if (hasWriting) xpService.earnXp(userId, XpSource.DAILY_TASK_COMPLETE, "t4-" + start.toString(), 10);
        tasks.add(DailyTask.builder()
                .id("t4")
                .title("Writing task")
                .description("Write an essay or report")
                .completed(hasWriting)
                .type("WRITING")
                .build());

        if (vocabCompleted && hasIelts && hasSpeaking && hasWriting) {
            xpService.earnXp(userId, XpSource.ALL_DAILY_TASKS_BONUS, "all-" + start.toString(), 20);
        }

        return tasks;
    }

    private ProgressOverview getProgressOverview(UUID userId) {
        // Band progress
        ScoreProgress listening = new ScoreProgress(0f, 0f);
        ScoreProgress reading = new ScoreProgress(0f, 0f);

        Optional<IeltsTestAttempt> latestIelts = ieltsTestAttemptRepository.findFirstByUserIdAndStatusOrderByCompletedAtDesc(userId, IeltsTestAttempt.AttemptStatus.COMPLETED);
        if (latestIelts.isPresent() && latestIelts.get().getBandScore() != null) {
            // Simplified for MVP, we use the same score for L/R/Overall from the single latest test
            Float score = latestIelts.get().getBandScore();
            listening.setCurrent(score);
            reading.setCurrent(score);
        }

        ScoreProgress speaking = new ScoreProgress(0f, 0f);
        Optional<SpeakingAttempt> latestSpeaking = speakingAttemptRepository.findFirstByUserIdAndStatusOrderByGradedAtDesc(userId, SpeakingAttempt.AttemptStatus.GRADED);
        if (latestSpeaking.isPresent() && latestSpeaking.get().getOverallBandScore() != null) {
            speaking.setCurrent(latestSpeaking.get().getOverallBandScore());
        }

        ScoreProgress writing = new ScoreProgress(0f, 0f);
        Optional<WritingSubmission> latestWriting = writingSubmissionRepository.findFirstByUserIdAndStatusOrderByGradedAtDesc(userId, WritingSubmission.SubmissionStatus.GRADED);
        if (latestWriting.isPresent() && latestWriting.get().getOverallBandScore() != null) {
            writing.setCurrent(latestWriting.get().getOverallBandScore());
        }

        // Calculate overall average
        int count = 0;
        float total = 0f;
        if (listening.getCurrent() != null && listening.getCurrent() > 0) {
            total += listening.getCurrent();
            count++;
        }
        if (speaking.getCurrent() != null && speaking.getCurrent() > 0) {
            total += speaking.getCurrent();
            count++;
        }
        if (writing.getCurrent() != null && writing.getCurrent() > 0) {
            total += writing.getCurrent();
            count++;
        }
        Float overallAvg = count > 0 ? (float) (Math.round((total / count) * 2) / 2.0) : 0f;

        BandProgress bandProgress = BandProgress.builder()
                .listening(listening)
                .reading(reading)
                .speaking(speaking)
                .writing(writing)
                .overall(new ScoreProgress(overallAvg, 0f))
                .build();

        // Vocab progress
        long totalWords = vocabularyRecordRepository.countAllUniqueWords(userId);
        long reviewingWords = vocabularyRecordRepository.countUniqueWrongWords(userId);
        long masteredWords = Math.max(0, totalWords - reviewingWords); // Simple approximation for MVP

        VocabProgress vocabProgress = VocabProgress.builder()
                .totalWords(totalWords)
                .masteredWords(masteredWords)
                .reviewingWords(reviewingWords)
                .build();

        return ProgressOverview.builder()
                .bandProgress(bandProgress)
                .vocabProgress(vocabProgress)
                .build();
    }

    private List<RecentActivity> getRecentActivities(UUID userId) {
        // We will fetch from UserActivityLogRepository. But since we didn't add the method to the repository in the multi-replace, we fetch all and limit via Stream for now to be safe, or we can use custom queries.
        // Given we don't have a reliable top N query in standard MVP without pagination, we'll manually fetch recent attempts
        // For production, UserActivityLog integration is better.

        List<RecentActivity> activities = new ArrayList<>();

        // Just pull last 5 from Vocabulary
        List<VocabularyRecord> vocab = vocabularyRecordRepository.findByUserIdOrderByTestedAtDesc(userId);
        if (!vocab.isEmpty()) {
            VocabularyRecord v = vocab.get(0);
            activities.add(RecentActivity.builder()
                    .id(v.getId().toString())
                    .title("Vocabulary Practice")
                    .type("VOCAB")
                    .score(v.getIsCorrect() ? "Correct" : "Incorrect")
                    .description("Word: " + v.getEnglishWord())
                    .timestamp(v.getTestedAt())
                    .build());
        }

        List<IeltsTestAttempt> ielts = ieltsTestAttemptRepository.findByUserIdOrderByStartedAtDesc(userId);
        if (!ielts.isEmpty()) {
            IeltsTestAttempt i = ielts.get(0);
            activities.add(RecentActivity.builder()
                    .id(i.getId().toString())
                    .title("IELTS Test")
                    .type("LISTENING")
                    .score(i.getBandScore() != null ? String.valueOf(i.getBandScore()) : "Pending")
                    .description(i.getCorrectCount() + "/" + i.getTotalQuestions() + " correct")
                    .timestamp(i.getStartedAt())
                    .build());
        }

        List<SpeakingAttempt> speakings = speakingAttemptRepository.findByUserIdOrderBySubmittedAtDesc(userId);
        if (!speakings.isEmpty()) {
            SpeakingAttempt s = speakings.get(0);
            activities.add(RecentActivity.builder()
                    .id(s.getId().toString())
                    .title("Speaking Practice")
                    .type("SPEAKING")
                    .score(s.getOverallBandScore() != null ? String.valueOf(s.getOverallBandScore()) : "Pending")
                    .description("Topic ID: " + s.getTopicId())
                    .timestamp(s.getSubmittedAt())
                    .build());
        }

        // Sort by timestamp desc and take top 5
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return activities.size() > 5 ? activities.subList(0, 5) : activities;
    }

    private List<QuickPracticeItem> generateQuickPractice() {
        return List.of(
                QuickPracticeItem.builder()
                        .id("q1")
                        .title("5 Vocabulary Flashcards")
                        .type("VOCAB")
                        .estimatedTime("2 mins")
                        .icon("BookOutlined")
                        .path("/")
                        .build(),
                QuickPracticeItem.builder()
                        .id("q2")
                        .title("3-minute Listening")
                        .type("LISTENING")
                        .estimatedTime("3 mins")
                        .icon("AudioOutlined")
                        .path("/ielts")
                        .build(),
                QuickPracticeItem.builder()
                        .id("q3")
                        .title("1 Speaking Question")
                        .type("SPEAKING")
                        .estimatedTime("5 mins")
                        .icon("MicOutlined")
                        .path("/speaking")
                        .build()
        );
    }
}
