package com.swpts.enpracticebe.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mascot_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MascotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Type(JsonBinaryType.class)
    @Column(name = "messages", columnDefinition = "jsonb")
    @Builder.Default
    private List<MessageItem> messages = new ArrayList<>();

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageItem {
        private String text;
        /** ENCOURAGEMENT, TIP, STREAK, COMEBACK, CELEBRATE */
        private String type;
        /** idle, happy, excited, thinking, celebrate, explain */
        private String mood;
    }
}
