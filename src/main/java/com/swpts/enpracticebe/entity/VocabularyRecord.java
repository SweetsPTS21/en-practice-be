package com.swpts.enpracticebe.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Table(name = "vocabulary_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "english_word", nullable = false)
    private String englishWord;

    @Column(name = "user_meaning", nullable = false)
    private String userMeaning;

    @Column(name = "correct_meaning", nullable = false)
    private String correctMeaning;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> alternatives = new ArrayList<>();

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> synonyms = new ArrayList<>();

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "tested_at")
    private Instant testedAt;

    @PrePersist
    protected void onCreate() {
        if (testedAt == null) {
            testedAt = Instant.now();
        }
    }

    /**
     * Frontend uses "timestamp" field name, DB uses "tested_at".
     */
    @JsonProperty("timestamp")
    public Instant getTimestamp() {
        return testedAt;
    }
}
