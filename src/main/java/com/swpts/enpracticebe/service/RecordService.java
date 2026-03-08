package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.vocabulary.ImportRequest;
import com.swpts.enpracticebe.dto.request.vocabulary.ListRecordRequest;
import com.swpts.enpracticebe.dto.request.vocabulary.RecordRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.vocabulary.*;

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
