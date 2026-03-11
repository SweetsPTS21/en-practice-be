package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.request.dictionary.AddDictionaryWordRequest;
import com.swpts.enpracticebe.dto.request.dictionary.ReviewWordRequest;
import com.swpts.enpracticebe.dto.request.dictionary.UpdateDictionaryWordRequest;
import com.swpts.enpracticebe.dto.response.dictionary.DictionaryStatsResponse;
import com.swpts.enpracticebe.dto.response.dictionary.DictionaryWordResponse;
import com.swpts.enpracticebe.entity.UserDictionary;
// Assuming we might need to change this if it's in a different package, let's grep for it first.
// Temporarily using RuntimeException to resolve compile error until I find the correct exception class.
import com.swpts.enpracticebe.repository.UserDictionaryRepository;
import com.swpts.enpracticebe.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

    private final UserDictionaryRepository dictionaryRepository;

    @Override
    @Transactional
    public DictionaryWordResponse addWord(UUID userId, AddDictionaryWordRequest request) {
        if (dictionaryRepository.existsByUserIdAndWordIgnoreCase(userId, request.getWord())) {
            throw new IllegalArgumentException("Word already exists in your dictionary");
        }

        UserDictionary userDictionary = UserDictionary.builder()
                .userId(userId)
                .word(request.getWord())
                .ipa(request.getIpa())
                .wordType(request.getWordType())
                .meaning(request.getMeaning())
                .explanation(request.getExplanation())
                .note(request.getNote())
                .examples(request.getExamples() != null ? request.getExamples() : new java.util.ArrayList<>())
                .tags(request.getTags() != null ? request.getTags() : new java.util.ArrayList<>())
                .sourceType(request.getSourceType() != null ? request.getSourceType() : com.swpts.enpracticebe.entity.DictionarySourceType.MANUAL)
                .isFavorite(request.getIsFavorite() != null ? request.getIsFavorite() : false)
                .proficiencyLevel(0)
                .reviewCount(0)
                .build();

        userDictionary = dictionaryRepository.save(userDictionary);
        return mapToResponse(userDictionary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DictionaryWordResponse> searchWords(UUID userId,
                                                    String keyword,
                                                    String wordType,
                                                    Boolean isFavorite,
                                                    Integer proficiencyLevel,
                                                    String tag,
                                                    Pageable pageable) {
        String jsonTag = tag != null ? "\"" + tag + "\"" : null;
        Page<UserDictionary> usersPage = dictionaryRepository.searchAdvanced(
                userId, keyword, wordType, isFavorite, proficiencyLevel, tag, jsonTag, pageable
        );
        return usersPage.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DictionaryWordResponse getWordById(UUID userId, UUID wordId) {
        UserDictionary userDictionary = getDictionaryWord(userId, wordId);
        return mapToResponse(userDictionary);
    }

    @Override
    @Transactional
    public DictionaryWordResponse updateWord(UUID userId, UUID wordId, UpdateDictionaryWordRequest request) {
        UserDictionary userDictionary = getDictionaryWord(userId, wordId);

        if (request.getWord() != null && !request.getWord().equalsIgnoreCase(userDictionary.getWord())) {
            if (dictionaryRepository.existsByUserIdAndWordIgnoreCase(userId, request.getWord())) {
                throw new IllegalArgumentException("Word already exists in your dictionary");
            }
            userDictionary.setWord(request.getWord());
        }

        if (request.getIpa() != null) userDictionary.setIpa(request.getIpa());
        if (request.getWordType() != null) userDictionary.setWordType(request.getWordType());
        if (request.getMeaning() != null) userDictionary.setMeaning(request.getMeaning());
        if (request.getExplanation() != null) userDictionary.setExplanation(request.getExplanation());
        if (request.getNote() != null) userDictionary.setNote(request.getNote());
        if (request.getExamples() != null) userDictionary.setExamples(request.getExamples());
        if (request.getTags() != null) userDictionary.setTags(request.getTags());
        if (request.getIsFavorite() != null) userDictionary.setIsFavorite(request.getIsFavorite());

        return mapToResponse(dictionaryRepository.save(userDictionary));
    }

    @Override
    @Transactional
    public void deleteWord(UUID userId, UUID wordId) {
        UserDictionary userDictionary = getDictionaryWord(userId, wordId);
        dictionaryRepository.delete(userDictionary);
    }

    @Override
    @Transactional
    public DictionaryWordResponse toggleFavorite(UUID userId, UUID wordId) {
        UserDictionary userDictionary = getDictionaryWord(userId, wordId);
        userDictionary.setIsFavorite(!userDictionary.getIsFavorite());
        return mapToResponse(dictionaryRepository.save(userDictionary));
    }

    @Override
    @Transactional
    public DictionaryWordResponse updateProficiency(UUID userId, UUID wordId, ReviewWordRequest request) {
        UserDictionary userDictionary = getDictionaryWord(userId, wordId);
        
        int score = request.getPerformanceScore();
        int currentLevel = userDictionary.getProficiencyLevel() != null ? userDictionary.getProficiencyLevel() : 0;
        
        // Simple SRS Logic
        if (score >= 3) {
            // Correct answer, increase level (max 5)
            currentLevel = Math.min(5, currentLevel + 1);
        } else {
            // Incorrect, reset or decrease level
            currentLevel = Math.max(0, currentLevel - 1);
        }
        
        userDictionary.setProficiencyLevel(currentLevel);
        userDictionary.setLastReviewedAt(Instant.now());
        userDictionary.setReviewCount((userDictionary.getReviewCount() != null ? userDictionary.getReviewCount() : 0) + 1);
        
        // Calculate next review time based on new level
        long hoursToNextReview = calculateNextReviewInterval(currentLevel);
        userDictionary.setNextReviewAt(Instant.now().plus(hoursToNextReview, ChronoUnit.HOURS));
        
        return mapToResponse(dictionaryRepository.save(userDictionary));
    }

    @Override
    @Transactional(readOnly = true)
    public DictionaryStatsResponse getDictionaryStats(UUID userId) {
        // We could write specific counts queries, but for simplicity we fetch stats via aggregation queries or count methods
        // To be precise and performant, we should add these to the repository.
        // For MVP, we will count by combining existing methods or writing new ones if needed.
        
        // A better approach is adding specific counts to repo or doing a custom query. 
        // Let's implement this efficiently by defining custom counts or fetching all and grouping (not recommended for large sets).
        // Let's assume we add methods to UserDictionaryRepository for these counts, or do a single aggregation query.
        
        // *Placeholder implementation* - Needs actual repo methods added for performance
        // For now, we will just return a mock or basic count to complete the structure. I will add the necessary repo methods next.
        
        long total = dictionaryRepository.countByUserId(userId);
        long favorite = dictionaryRepository.countByUserIdAndIsFavoriteTrue(userId);
        long newWords = dictionaryRepository.countByUserIdAndProficiencyLevel(userId, 0);
        long masterWords = dictionaryRepository.countByUserIdAndProficiencyLevelGreaterThanEqual(userId, 4);
        long learning = total - newWords - masterWords;
        long toReview = dictionaryRepository.countByUserIdAndNextReviewAtBefore(userId, Instant.now());

        return DictionaryStatsResponse.builder()
                .totalWords(total)
                .favoriteWords(favorite)
                .newWords(newWords)
                .learningWords(learning)
                .masteredWords(masterWords)
                .wordsToReviewToday(toReview)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DictionaryWordResponse> getWordsDueForReview(UUID userId, Pageable pageable) {
         Page<UserDictionary> usersPage = dictionaryRepository.findByUserIdAndNextReviewAtBefore(
                userId, Instant.now(), pageable
        );
        return usersPage.map(this::mapToResponse);
    }

    // --- Helper Methods ---
    
    private long calculateNextReviewInterval(int level) {
       return switch (level) {
           case 0 -> 4; // 4 hours
           case 1 -> 24; // 1 day
           case 2 -> 72; // 3 days
           case 3 -> 168; // 1 week
           case 4 -> 336; // 2 weeks
           case 5 -> 720; // 1 month
           default -> 24;
       };
    }

    private UserDictionary getDictionaryWord(UUID userId, UUID wordId) {
        UserDictionary word = dictionaryRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Dictionary word not found")); // Will update CustomException later
        
        if (!word.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this word");
        }
        return word;
    }

    private DictionaryWordResponse mapToResponse(UserDictionary entity) {
        return DictionaryWordResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .word(entity.getWord())
                .ipa(entity.getIpa())
                .wordType(entity.getWordType())
                .meaning(entity.getMeaning())
                .explanation(entity.getExplanation())
                .note(entity.getNote())
                .examples(entity.getExamples())
                .tags(entity.getTags())
                .sourceType(entity.getSourceType())
                .isFavorite(entity.getIsFavorite())
                .proficiencyLevel(entity.getProficiencyLevel())
                .lastReviewedAt(entity.getLastReviewedAt())
                .nextReviewAt(entity.getNextReviewAt())
                .reviewCount(entity.getReviewCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
