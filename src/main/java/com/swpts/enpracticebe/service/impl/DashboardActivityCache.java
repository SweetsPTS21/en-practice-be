package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.response.dashboard.RecentActivity;
import com.swpts.enpracticebe.entity.*;
import com.swpts.enpracticebe.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Separated into its own Spring-managed bean so that @Cacheable is properly
 * intercepted by Spring AOP (self-invocation inside the same class bypasses the proxy).
 *
 * Cache: "dashboardRecentActivities" — TTL 5 min (declared in CacheConfig).
 */
@Component
@RequiredArgsConstructor
public class DashboardActivityCache {

    private final VocabularyRecordRepository vocabularyRecordRepository;
    private final IeltsTestAttemptRepository ieltsTestAttemptRepository;
    private final SpeakingAttemptRepository speakingAttemptRepository;
    private final WritingSubmissionRepository writingSubmissionRepository;

    /**
     * Returns the 10 most recent activities for the given user, aggregated across all
     * practice types (Vocabulary, IELTS, Speaking, Writing).
     *
     * <p>Each source contributes at most 5 rows (bounded DB queries), so the maximum
     * number of candidates before sorting is 20, keeping memory usage minimal.</p>
     *
     * <p>Cached per userId for 5 minutes (Caffeine short-TTL pool).</p>
     */
    @Cacheable(value = "dashboardRecentActivities", key = "#userId.toString()")
    public List<RecentActivity> getRecentActivities(UUID userId) {
        List<RecentActivity> activities = new ArrayList<>();

        // 1. Vocabulary — top 5 rows only (bounded query, no full scan)
        List<VocabularyRecord> vocabList = vocabularyRecordRepository.findTop5ByUserIdOrderByTestedAtDesc(userId);
        for (VocabularyRecord v : vocabList) {
            boolean correct = Boolean.TRUE.equals(v.getIsCorrect());
            activities.add(RecentActivity.builder()
                    .id(v.getId().toString())
                    .title("Vocabulary Practice")
                    .type("VOCAB")
                    .score(correct ? "Correct" : "Incorrect")
                    .description((correct ? "✓ " : "✗ ") + v.getEnglishWord())
                    .timestamp(v.getTestedAt())
                    .build());
        }

        // 2. IELTS (Listening / Reading) — top 5 rows only
        List<IeltsTestAttempt> ieltsList = ieltsTestAttemptRepository.findTop5ByUserIdOrderByStartedAtDesc(userId);
        for (IeltsTestAttempt i : ieltsList) {
            String score = i.getBandScore() != null ? String.format("Band %.1f", i.getBandScore()) : "Pending";
            String desc = i.getCorrectCount() + "/" + i.getTotalQuestions() + " correct answers";
            Instant timestamp = i.getCompletedAt() != null ? i.getCompletedAt() : i.getStartedAt();
            activities.add(RecentActivity.builder()
                    .id(i.getId().toString())
                    .title("IELTS Test")
                    .type("LISTENING")
                    .score(score)
                    .description(desc)
                    .timestamp(timestamp)
                    .build());
        }

        // 3. Speaking — top 5 rows only
        List<SpeakingAttempt> speakingList = speakingAttemptRepository.findTop5ByUserIdOrderBySubmittedAtDesc(userId);
        for (SpeakingAttempt s : speakingList) {
            String score = s.getOverallBandScore() != null ? String.format("Band %.1f", s.getOverallBandScore()) : "Pending";
            String statusLabel = s.getStatus() != null ? s.getStatus().name() : "SUBMITTED";
            String desc = "Status: " + statusLabel
                    + (s.getOverallBandScore() != null ? " • Score: " + String.format("%.1f", s.getOverallBandScore()) : "");
            activities.add(RecentActivity.builder()
                    .id(s.getId().toString())
                    .title("Speaking Practice")
                    .type("SPEAKING")
                    .score(score)
                    .description(desc)
                    .timestamp(s.getSubmittedAt())
                    .build());
        }

        // 4. Writing — top 5 rows only (was completely missing before)
        List<WritingSubmission> writingList = writingSubmissionRepository.findTop5ByUserIdOrderBySubmittedAtDesc(userId);
        for (WritingSubmission w : writingList) {
            String score = w.getOverallBandScore() != null ? String.format("Band %.1f", w.getOverallBandScore()) : "Pending";
            String desc = w.getWordCount() + " words"
                    + (w.getOverallBandScore() != null ? " • Score: " + String.format("%.1f", w.getOverallBandScore()) : " • Pending review");
            Instant timestamp = w.getGradedAt() != null ? w.getGradedAt() : w.getSubmittedAt();
            activities.add(RecentActivity.builder()
                    .id(w.getId().toString())
                    .title("Writing Task")
                    .type("WRITING")
                    .score(score)
                    .description(desc)
                    .timestamp(timestamp)
                    .build());
        }

        // Merge all candidates, sort by timestamp desc, return top 10
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return activities.size() > 10 ? activities.subList(0, 10) : activities;
    }

    /**
     * Call this whenever the user completes a new activity to invalidate the stale cache
     * immediately instead of waiting for the 5-min TTL.
     */
    @CacheEvict(value = "dashboardRecentActivities", key = "#userId.toString()")
    public void evict(UUID userId) {
        // Intentionally empty — @CacheEvict handles the work
    }
}
