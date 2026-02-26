package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.ChartEntry;
import com.swpts.enpracticebe.dto.ReviewCountsDto;
import com.swpts.enpracticebe.dto.ReviewWordDto;
import com.swpts.enpracticebe.dto.request.ImportRequest;
import com.swpts.enpracticebe.dto.request.ListRecordRequest;
import com.swpts.enpracticebe.dto.request.RecordRequest;
import com.swpts.enpracticebe.dto.response.ImportResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.StatsResponse;
import com.swpts.enpracticebe.dto.response.VocabularyRecordResponse;

import java.util.List;
import java.util.UUID;

public interface RecordService {
    PageResponse<VocabularyRecordResponse> getAllRecords(ListRecordRequest request);

    VocabularyRecordResponse createRecord(RecordRequest request);

    void deleteRecord(UUID recordId);

    void deleteAllRecords();

    ImportResponse importData(ImportRequest request);

    StatsResponse getStats(String period);

    List<ChartEntry> getChartData(String period);

    int getStreak();

    List<ReviewWordDto> getReviewWords(String filter, int limit);

    ReviewCountsDto getReviewCounts();
}
