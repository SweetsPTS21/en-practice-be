package com.swpts.enpracticebe.entity;

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

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "target_ielts_band")
    private Float targetIeltsBand;

    @Column(name = "target_exam_date")
    private LocalDate targetExamDate;

    @Column(name = "daily_goal_minutes", nullable = false)
    @Builder.Default
    private Integer dailyGoalMinutes = 30;

    @Column(name = "weekly_word_goal")
    private Integer weeklyWordGoal;

    @Column(name = "preferred_skill", length = 50)
    private String preferredSkill;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (dailyGoalMinutes == null) {
            dailyGoalMinutes = 30;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        if (dailyGoalMinutes == null) {
            dailyGoalMinutes = 30;
        }
    }
}
