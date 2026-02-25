package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.*;
import com.swpts.enpracticebe.entity.VocabularyRecord;
import com.swpts.enpracticebe.service.RecordService;
import com.swpts.enpracticebe.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/records")
public class RecordController {
    private final RecordService recordService;
    private final AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<List<VocabularyRecord>> getAllRecords(Authentication auth) {
        UUID userId = authUtil.getUserId();
        return ResponseEntity.ok(recordService.getAllRecords(userId));
    }

    @PostMapping
    public ResponseEntity<VocabularyRecord> createRecord(Authentication auth,
                                                         @Valid @RequestBody RecordRequest request) {
        UUID userId = authUtil.getUserId();
        VocabularyRecord record = recordService.createRecord(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable UUID id) {
        UUID userId = authUtil.getUserId();
        recordService.deleteRecord(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllRecords() {
        UUID userId = authUtil.getUserId();
        recordService.deleteAllRecords(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResponse> importData(@RequestBody ImportRequest request) {
        UUID userId = authUtil.getUserId();
        ImportResponse response = recordService.importData(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats(@RequestParam(defaultValue = "day") String period) {
        UUID userId = authUtil.getUserId();
        return ResponseEntity.ok(recordService.getStats(userId, period));
    }

    @GetMapping("/chart")
    public ResponseEntity<List<ChartEntry>> getChartData(@RequestParam(defaultValue = "day") String period) {
        UUID userId = authUtil.getUserId();
        return ResponseEntity.ok(recordService.getChartData(userId, period));
    }

    @GetMapping("/streak")
    public ResponseEntity<Integer> getStreak() {
        UUID userId = authUtil.getUserId();
        return ResponseEntity.ok(recordService.getStreak(userId));
    }

    @GetMapping("/review-words")
    public ResponseEntity<List<ReviewWordDto>> getReviewWords(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "20") int limit) {
        UUID userId = authUtil.getUserId();
        return ResponseEntity.ok(recordService.getReviewWords(userId, filter, limit));
    }

    @GetMapping("/review-counts")
    public ResponseEntity<ReviewCountsDto> getReviewCounts(Authentication auth) {
        UUID userId = authUtil.getUserId();
        return ResponseEntity.ok(recordService.getReviewCounts(userId));
    }
}
