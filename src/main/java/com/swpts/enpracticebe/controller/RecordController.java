package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.*;
import com.swpts.enpracticebe.entity.VocabularyRecord;
import com.swpts.enpracticebe.service.RecordService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/records")
public class RecordController {
    private final RecordService recordService;

    @GetMapping
    public ResponseEntity<List<VocabularyRecord>> getAllRecords() {
        return ResponseEntity.ok(recordService.getAllRecords());
    }

    @PostMapping
    public ResponseEntity<VocabularyRecord> createRecord(@Valid @RequestBody RecordRequest request) {
        VocabularyRecord record = recordService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable UUID id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllRecords() {
        recordService.deleteAllRecords();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResponse> importData(@RequestBody ImportRequest request) {
        ImportResponse response = recordService.importData(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats(@RequestParam(defaultValue = "day") String period) {
        return ResponseEntity.ok(recordService.getStats(period));
    }

    @GetMapping("/chart")
    public ResponseEntity<List<ChartEntry>> getChartData(@RequestParam(defaultValue = "day") String period) {
        return ResponseEntity.ok(recordService.getChartData(period));
    }

    @GetMapping("/streak")
    public ResponseEntity<Integer> getStreak() {
        return ResponseEntity.ok(recordService.getStreak());
    }

    @GetMapping("/review-words")
    public ResponseEntity<List<ReviewWordDto>> getReviewWords(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(recordService.getReviewWords(filter, limit));
    }

    @GetMapping("/review-counts")
    public ResponseEntity<ReviewCountsDto> getReviewCounts() {
        return ResponseEntity.ok(recordService.getReviewCounts());
    }
}
