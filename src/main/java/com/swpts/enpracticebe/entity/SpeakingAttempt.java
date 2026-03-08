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
