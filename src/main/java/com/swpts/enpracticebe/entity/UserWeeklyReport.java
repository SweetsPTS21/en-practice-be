package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_weekly_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWeeklyReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "week_start", nullable = false)
    private Instant weekStart;

    @Column(name = "week_end", nullable = false)
    private Instant weekEnd;

    @Column(name = "study_minutes")
    private Integer studyMinutes;

    @Column(name = "vocabulary_learned")
    private Integer vocabularyLearned;

    @Column(name = "tests_completed")
    private Integer testsCompleted;

    @Column(name = "band_improvement")
    private Float bandImprovement;
}
