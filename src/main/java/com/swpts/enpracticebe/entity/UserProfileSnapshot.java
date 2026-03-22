package com.swpts.enpracticebe.entity;

import com.swpts.enpracticebe.dto.response.dashboard.DayStatus;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_profile_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileSnapshot {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "total_xp", nullable = false)
    @Builder.Default
    private Integer totalXp = 0;

    @Column(name = "current_level", nullable = false)
    @Builder.Default
    private Integer currentLevel = 1;

    @Column(name = "current_level_min_xp", nullable = false)
    @Builder.Default
    private Integer currentLevelMinXp = 0;

    @Column(name = "next_level", nullable = false)
    @Builder.Default
    private Integer nextLevel = 2;

    @Column(name = "next_level_min_xp", nullable = false)
    @Builder.Default
    private Integer nextLevelMinXp = 100;

    @Column(name = "xp_into_current_level", nullable = false)
    @Builder.Default
    private Integer xpIntoCurrentLevel = 0;

    @Column(name = "xp_needed_for_next_level", nullable = false)
    @Builder.Default
    private Integer xpNeededForNextLevel = 100;

    @Column(name = "level_progress_percentage", nullable = false)
    @Builder.Default
    private Integer levelProgressPercentage = 0;

    @Column(name = "weekly_xp", nullable = false)
    @Builder.Default
    private Integer weeklyXp = 0;

    @Column(name = "total_lessons_completed", nullable = false)
    @Builder.Default
    private Long totalLessonsCompleted = 0L;

    @Column(name = "total_words_learned", nullable = false)
    @Builder.Default
    private Long totalWordsLearned = 0L;

    @Column(name = "total_study_minutes", nullable = false)
    @Builder.Default
    private Integer totalStudyMinutes = 0;

    @Column(name = "studied_minutes_today", nullable = false)
    @Builder.Default
    private Integer studiedMinutesToday = 0;

    @Column(name = "words_to_review_today", nullable = false)
    @Builder.Default
    private Long wordsToReviewToday = 0L;

    @Column(name = "favorite_words", nullable = false)
    @Builder.Default
    private Long favoriteWords = 0L;

    @Column(name = "new_words", nullable = false)
    @Builder.Default
    private Long newWords = 0L;

    @Column(name = "learning_words", nullable = false)
    @Builder.Default
    private Long learningWords = 0L;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    @Builder.Default
    private Integer longestStreak = 0;

    @Column(name = "active_days_last_30", nullable = false)
    @Builder.Default
    private Integer activeDaysLast30 = 0;

    @Type(JsonBinaryType.class)
    @Column(name = "last_30_days_heatmap", columnDefinition = "jsonb")
    @Builder.Default
    private List<DayStatus> last30DaysHeatmap = new ArrayList<>();

    @Column(name = "listening_band")
    private Float listeningBand;

    @Column(name = "reading_band")
    private Float readingBand;

    @Column(name = "speaking_band")
    private Float speakingBand;

    @Column(name = "writing_band")
    private Float writingBand;

    @Column(name = "overall_band")
    private Float overallBand;

    @Column(name = "vocab_total_words", nullable = false)
    @Builder.Default
    private Long vocabTotalWords = 0L;

    @Column(name = "vocab_mastered_words", nullable = false)
    @Builder.Default
    private Long vocabMasteredWords = 0L;

    @Column(name = "vocab_reviewing_words", nullable = false)
    @Builder.Default
    private Long vocabReviewingWords = 0L;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (computedAt == null) {
            computedAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
