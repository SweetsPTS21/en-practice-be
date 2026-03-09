package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "dashboard_daily_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDailyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "stat_date", nullable = false, unique = true)
    private LocalDate statDate;

    @Column(name = "total_users")
    @Builder.Default
    private long totalUsers = 0;

    @Column(name = "active_users_today")
    @Builder.Default
    private long activeUsersToday = 0;

    @Column(name = "new_users_this_week")
    @Builder.Default
    private long newUsersThisWeek = 0;

    @Column(name = "total_ielts")
    @Builder.Default
    private long totalIelts = 0;

    @Column(name = "published_ielts")
    @Builder.Default
    private long publishedIelts = 0;

    @Column(name = "total_speaking")
    @Builder.Default
    private long totalSpeaking = 0;

    @Column(name = "published_speaking")
    @Builder.Default
    private long publishedSpeaking = 0;

    @Column(name = "total_writing")
    @Builder.Default
    private long totalWriting = 0;

    @Column(name = "published_writing")
    @Builder.Default
    private long publishedWriting = 0;

    @Column(name = "total_attempts")
    @Builder.Default
    private long totalAttempts = 0;

    @Column(name = "attempts_today")
    @Builder.Default
    private long attemptsToday = 0;

    @Column(name = "vocab_today")
    @Builder.Default
    private long vocabToday = 0;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
