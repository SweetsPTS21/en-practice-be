package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "writing_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingSubmission {

    public enum SubmissionStatus {
        SUBMITTED, GRADING, GRADED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "essay_content", nullable = false, columnDefinition = "TEXT")
    private String essayContent;

    @Column(name = "word_count", nullable = false)
    @Builder.Default
    private Integer wordCount = 0;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(name = "task_response_score")
    private Float taskResponseScore;

    @Column(name = "coherence_score")
    private Float coherenceScore;

    @Column(name = "lexical_resource_score")
    private Float lexicalResourceScore;

    @Column(name = "grammar_score")
    private Float grammarScore;

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
