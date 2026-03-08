package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "speaking_conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingConversation {

    public enum ConversationStatus {
        IN_PROGRESS, COMPLETED, GRADING, GRADED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "topic_id", nullable = false)
    private UUID topicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.IN_PROGRESS;

    @Column(name = "total_turns")
    @Builder.Default
    private Integer totalTurns = 0;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

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

    @Column(name = "started_at", updatable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "graded_at")
    private Instant gradedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) startedAt = Instant.now();
    }
}
