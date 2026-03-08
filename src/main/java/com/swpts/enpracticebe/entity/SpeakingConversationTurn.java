package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "speaking_conversation_turns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingConversationTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Column(name = "ai_question", nullable = false, columnDefinition = "TEXT")
    private String aiQuestion;

    @Column(name = "user_transcript", columnDefinition = "TEXT")
    private String userTranscript;

    @Column(name = "audio_url", columnDefinition = "TEXT")
    private String audioUrl;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Column(name = "turn_type")
    @Builder.Default
    private String turnType = "QUESTION"; // QUESTION or HINT

    @Column(name = "follow_up_index")
    private Integer followUpIndex; // which follow-up this turn addresses

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
