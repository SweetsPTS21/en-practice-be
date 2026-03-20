package com.swpts.enpracticebe.entity;

import com.swpts.enpracticebe.constant.CustomConversationExpertise;
import com.swpts.enpracticebe.constant.CustomConversationPersonality;
import com.swpts.enpracticebe.constant.CustomConversationStyle;
import com.swpts.enpracticebe.constant.VoiceName;
import com.swpts.enpracticebe.entity.converter.VoiceNameConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "custom_speaking_conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomSpeakingConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    @Column(name = "topic", nullable = false, columnDefinition = "TEXT")
    private String topic;
    @Enumerated(EnumType.STRING)
    @Column(name = "style", nullable = false, length = 30)
    private CustomConversationStyle style;
    @Enumerated(EnumType.STRING)
    @Column(name = "personality", nullable = false, length = 30)
    private CustomConversationPersonality personality;
    @Enumerated(EnumType.STRING)
    @Column(name = "expertise", nullable = false, length = 30)
    private CustomConversationExpertise expertise;
    @Convert(converter = VoiceNameConverter.class)
    @Column(name = "voice_name", nullable = false, length = 30)
    private VoiceName voiceName;
    @Column(name = "grading_enabled", nullable = false)
    @Builder.Default
    private Boolean gradingEnabled = false;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.IN_PROGRESS;
    @Column(name = "max_user_turns", nullable = false)
    private Integer maxUserTurns;
    @Column(name = "user_turn_count", nullable = false)
    @Builder.Default
    private Integer userTurnCount = 0;
    @Column(name = "total_turns", nullable = false)
    @Builder.Default
    private Integer totalTurns = 0;
    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;
    @Column(name = "fluency_score")
    private Float fluencyScore;
    @Column(name = "vocabulary_score")
    private Float vocabularyScore;
    @Column(name = "coherence_score")
    private Float coherenceScore;
    @Column(name = "pronunciation_score")
    private Float pronunciationScore;
    @Column(name = "overall_score")
    private Float overallScore;
    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;
    @Column(name = "system_prompt_snapshot", columnDefinition = "TEXT")
    private String systemPromptSnapshot;
    @Column(name = "started_at", updatable = false)
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "graded_at")
    private Instant gradedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = Instant.now();
        }
    }

    public enum ConversationStatus {
        IN_PROGRESS, COMPLETED, GRADING, GRADED, FAILED
    }
}
