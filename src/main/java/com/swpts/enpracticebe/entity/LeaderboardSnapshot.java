package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leaderboard_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "period_type", length = 20, nullable = false)
    private String periodType;

    @Column(name = "period_key", length = 20, nullable = false)
    private String periodKey;

    @Column(name = "scope", length = 30, nullable = false)
    @Builder.Default
    private String scope = "GLOBAL";

    @Column(name = "xp", nullable = false)
    @Builder.Default
    private Integer xp = 0;

    @Column(name = "rank", nullable = false)
    @Builder.Default
    private Integer rank = 0;

    @Column(name = "previous_rank")
    private Integer previousRank;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
