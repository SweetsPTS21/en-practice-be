package com.swpts.enpracticebe.dto.response.dictionary;

import com.swpts.enpracticebe.entity.DictionarySourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryWordResponse {
    private UUID id;
    private UUID userId;
    private String word;
    private String ipa;
    private String wordType;
    private String meaning;
    private String explanation;
    private String note;
    private List<ExampleSentence> examples;
    private List<String> tags;
    private DictionarySourceType sourceType;
    private Boolean isFavorite;

    // Learning tracking
    private Integer proficiencyLevel;
    private Instant lastReviewedAt;
    private Instant nextReviewAt;
    private Integer reviewCount;

    private Instant createdAt;
    private Instant updatedAt;
}
