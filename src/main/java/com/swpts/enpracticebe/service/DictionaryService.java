package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.dictionary.AddDictionaryWordRequest;
import com.swpts.enpracticebe.dto.request.dictionary.ReviewWordRequest;
import com.swpts.enpracticebe.dto.request.dictionary.UpdateDictionaryWordRequest;
import com.swpts.enpracticebe.dto.response.dictionary.DictionaryStatsResponse;
import com.swpts.enpracticebe.dto.response.dictionary.DictionaryWordResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DictionaryService {

    DictionaryWordResponse addWord(UUID userId, AddDictionaryWordRequest request);

    Page<DictionaryWordResponse> searchWords(
            UUID userId,
            String keyword,
            String wordType,
            Boolean isFavorite,
            Integer proficiencyLevel,
            String tag,
            Pageable pageable
    );

    DictionaryWordResponse getWordById(UUID userId, UUID wordId);

    DictionaryWordResponse updateWord(UUID userId, UUID wordId, UpdateDictionaryWordRequest request);

    void deleteWord(UUID userId, UUID wordId);

    DictionaryWordResponse toggleFavorite(UUID userId, UUID wordId);

    DictionaryWordResponse updateProficiency(UUID userId, UUID wordId, ReviewWordRequest request);

    DictionaryStatsResponse getDictionaryStats(UUID userId);

    Page<DictionaryWordResponse> getWordsDueForReview(UUID userId, Pageable pageable);
}
