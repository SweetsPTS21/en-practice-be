package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_xp_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserXpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "source", length = 50, nullable = false)
    private String source;

    @Column(name = "source_id", length = 100)
    private String sourceId;

    @Column(name = "xp_amount", nullable = false)
    @Builder.Default
    private Integer xpAmount = 0;

    @Column(name = "earned_at", nullable = false)
    @Builder.Default
    private Instant earnedAt = Instant.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
