package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.ChartEntry;
import com.swpts.enpracticebe.dto.ReviewCountsDto;
import com.swpts.enpracticebe.dto.ReviewWordDto;
import com.swpts.enpracticebe.dto.request.ImportRequest;
import com.swpts.enpracticebe.dto.request.ListRecordRequest;
import com.swpts.enpracticebe.dto.request.RecordRequest;
import com.swpts.enpracticebe.dto.response.*;
import com.swpts.enpracticebe.service.RecordService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/records")
public class RecordController {
    private final RecordService recordService;

    @PostMapping("/search")
    public DefaultResponse<PageResponse<VocabularyRecordResponse>> getAllRecords(@RequestBody ListRecordRequest request) {
        return DefaultResponse.success(recordService.getAllRecords(request));
    }

    @PostMapping
    public DefaultResponse<VocabularyRecordResponse> createRecord(@Valid @RequestBody RecordRequest request) {
        return DefaultResponse.success(recordService.createRecord(request));
    }

    @DeleteMapping("/{id}")
    public DefaultResponse<Void> deleteRecord(@PathVariable UUID id) {
        recordService.deleteRecord(id);
        return DefaultResponse.success("Xóa từ thành công");
    }

    @DeleteMapping
    public DefaultResponse<Void> deleteAllRecords() {
        recordService.deleteAllRecords();
        return DefaultResponse.success("Xóa tất cả lịch sử thành công");
    }

    @PostMapping("/import")
    public DefaultResponse<ImportResponse> importData(@RequestBody ImportRequest request) {
        ImportResponse response = recordService.importData(request);
        return DefaultResponse.success("Import thành công", response);
    }

    @GetMapping("/stats")
    public DefaultResponse<StatsResponse> getStats(@RequestParam(defaultValue = "day") String period) {
        return DefaultResponse.success(recordService.getStats(period));
    }

    @GetMapping("/chart")
    public DefaultResponse<List<ChartEntry>> getChartData(@RequestParam(defaultValue = "day") String period) {
        return DefaultResponse.success(recordService.getChartData(period));
    }

    @GetMapping("/streak")
    public DefaultResponse<Integer> getStreak() {
        return DefaultResponse.success(recordService.getStreak());
    }

    @GetMapping("/review-words")
    public DefaultResponse<List<ReviewWordDto>> getReviewWords(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "20") int limit) {
        return DefaultResponse.success(recordService.getReviewWords(filter, limit));
    }

    @GetMapping("/review-counts")
    public DefaultResponse<ReviewCountsDto> getReviewCounts() {
        return DefaultResponse.success(recordService.getReviewCounts());
    }
}
