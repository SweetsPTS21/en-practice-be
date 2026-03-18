package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "speaking_attempts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingAttempt {

    public enum AttemptStatus {
        SUBMITTED, GRADING, GRADED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "topic_id", nullable = false)
    private UUID topicId;

    @Column(name = "audio_url", columnDefinition = "TEXT")
    private String audioUrl;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.SUBMITTED;

    @Column(name = "fluency_score")
    private Float fluencyScore;

    @Column(name = "lexical_score")
    private Float lexicalScore;

    @Column(name = "grammar_score")
    private Float grammarScore;

    @Column(name = "pronunciation_score")
    private Float pronunciationScore;

    @Column(name = "overall_band_score")
    private Float overallBandScore;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    // ─── Speech analytics (from Google STT word-level data) ───────────────────
    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "words_per_minute")
    private Double wordsPerMinute;

    @Column(name = "pause_count")
    private Integer pauseCount;

    @Column(name = "avg_pause_duration_ms")
    private Double avgPauseDurationMs;

    @Column(name = "long_pause_count")
    private Integer longPauseCount;

    @Column(name = "filler_word_count")
    private Integer fillerWordCount;

    @Column(name = "avg_word_confidence")
    private Double avgWordConfidence;

    /**
     * Full JSON representation of {@link com.swpts.enpracticebe.dto.speech.SpeechAnalyticsDto}.
     * Contains filler_words[], low_confidence_words[], and per-word wordDetails[].
     */
    @Column(name = "speech_data_json", columnDefinition = "TEXT")
    private String speechDataJson;

    @Column(name = "submitted_at", updatable = false)
    private Instant submittedAt;

    @Column(name = "graded_at")
    private Instant gradedAt;

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = Instant.now();
        }
    }
}
