package com.swpts.enpracticebe.dto.response.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryStatsResponse {
    private long totalWords;
    private long favoriteWords;
    private long wordsToReviewToday;
    private long newWords; // proficiencyLevel = 0
    private long learningWords; // proficiencyLevel 1-3
    private long masteredWords; // proficiencyLevel 4-5
}
