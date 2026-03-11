package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.dictionary.AddDictionaryWordRequest;
import com.swpts.enpracticebe.dto.request.dictionary.ReviewWordRequest;
import com.swpts.enpracticebe.dto.request.dictionary.UpdateDictionaryWordRequest;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.dictionary.DictionaryStatsResponse;
import com.swpts.enpracticebe.dto.response.dictionary.DictionaryWordResponse;
import com.swpts.enpracticebe.service.DictionaryService;
import com.swpts.enpracticebe.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<DefaultResponse<DictionaryWordResponse>> addWord(
            @Valid @RequestBody AddDictionaryWordRequest request) {
        DictionaryWordResponse response = dictionaryService.addWord(authUtil.getUserId(), request);
        return ResponseEntity.ok(DefaultResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<DefaultResponse<Page<DictionaryWordResponse>>> searchWords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String wordType,
            @RequestParam(required = false) Boolean isFavorite,
            @RequestParam(required = false) Integer proficiencyLevel,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DictionaryWordResponse> response = dictionaryService.searchWords(
                authUtil.getUserId(), keyword, wordType, isFavorite, proficiencyLevel, tag, pageable);
        return ResponseEntity.ok(DefaultResponse.success(response));
    }

    @GetMapping("/stats")
    public ResponseEntity<DefaultResponse<DictionaryStatsResponse>> getStats() {
        DictionaryStatsResponse response = dictionaryService.getDictionaryStats(authUtil.getUserId());
        return ResponseEntity.ok(DefaultResponse.success(response));
    }

    @GetMapping("/due-review")
    public ResponseEntity<DefaultResponse<Page<DictionaryWordResponse>>> getDueReview(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nextReviewAt").ascending());
        Page<DictionaryWordResponse> response = dictionaryService.getWordsDueForReview(authUtil.getUserId(), pageable);
        return ResponseEntity.ok(DefaultResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<DictionaryWordResponse>> getWord(@PathVariable UUID id) {
        DictionaryWordResponse response = dictionaryService.getWordById(authUtil.getUserId(), id);
        return ResponseEntity.ok(DefaultResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DefaultResponse<DictionaryWordResponse>> updateWord(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDictionaryWordRequest request) {
        DictionaryWordResponse response = dictionaryService.updateWord(authUtil.getUserId(), id, request);
        return ResponseEntity.ok(DefaultResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DefaultResponse<Void>> deleteWord(@PathVariable UUID id) {
        dictionaryService.deleteWord(authUtil.getUserId(), id);
        return ResponseEntity.ok(DefaultResponse.success(null)); // Check if .success(null) or a specific constructor is better in this project. Using success(null) as it exists in line 55.
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<DefaultResponse<DictionaryWordResponse>> toggleFavorite(@PathVariable UUID id) {
        DictionaryWordResponse response = dictionaryService.toggleFavorite(authUtil.getUserId(), id);
        return ResponseEntity.ok(DefaultResponse.success(response));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<DefaultResponse<DictionaryWordResponse>> updateProficiency(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewWordRequest request) {
        DictionaryWordResponse response = dictionaryService.updateProficiency(authUtil.getUserId(), id, request);
        return ResponseEntity.ok(DefaultResponse.success(response));
    }
}
