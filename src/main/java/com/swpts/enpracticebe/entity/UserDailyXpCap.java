package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_daily_xp_cap")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDailyXpCap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_xp_earned", nullable = false)
    @Builder.Default
    private Integer totalXpEarned = 0;
}
