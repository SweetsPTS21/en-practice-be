package com.swpts.enpracticebe.entity;

import com.swpts.enpracticebe.entity.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "speaking_topics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Part part;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;
    @Column(name = "cue_card", columnDefinition = "TEXT")
    private String cueCard;
    @Convert(converter = StringListConverter.class)
    @Column(name = "follow_up_questions", columnDefinition = "TEXT")
    private List<String> followUpQuestions;
    @Column(name = "ai_grading_prompt", columnDefinition = "TEXT")
    private String aiGradingPrompt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Difficulty difficulty = Difficulty.MEDIUM;
    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum Part {
        PART_1, PART_2, PART_3
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}
