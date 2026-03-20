package com.swpts.enpracticebe.entity;

import com.swpts.enpracticebe.constant.DictionarySourceType;
import com.swpts.enpracticebe.dto.response.dictionary.ExampleSentence;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_dictionary", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "word"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Core Word Data
    @Column(nullable = false, length = 200)
    private String word;

    @Column(length = 200)
    private String ipa;

    @Column(name = "word_type", length = 50)
    private String wordType;

    // Meaning & Usage
    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<ExampleSentence> examples = new ArrayList<>();

    // Organization & Metadata
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 50)
    @Builder.Default
    private DictionarySourceType sourceType = DictionarySourceType.MANUAL;

    @Column(name = "source_reference_id")
    private UUID sourceReferenceId;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private Boolean isFavorite = false;

    // Learning/SRS Tracking
    @Column(name = "proficiency_level", nullable = false)
    @Builder.Default
    private Integer proficiencyLevel = 0;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "next_review_at")
    private Instant nextReviewAt;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
