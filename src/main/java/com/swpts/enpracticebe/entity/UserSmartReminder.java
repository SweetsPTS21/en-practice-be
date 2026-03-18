package com.swpts.enpracticebe.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_smart_reminders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSmartReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    /**
     * AI-generated reminder payload stored as JSONB.
     * Mirrors the SmartReminder DTO structure for easy mapping.
     */
    @Type(JsonBinaryType.class)
    @Column(name = "reminder", columnDefinition = "jsonb")
    private ReminderPayload reminder;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReminderPayload {
        private String title;
        private String message;
        /** WARNING | INFO | SUCCESS | TIP */
        private String type;
        private String ctaText;
        private String ctaPath;
    }
}
