package com.swpts.enpracticebe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRequest {

    private List<ImportRecordDto> records = new ArrayList<>();
    private List<ImportReviewSessionDto> reviewSessions = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportRecordDto {
        private String id;
        private String englishWord;
        private String userMeaning;
        private String correctMeaning;
        private List<String> alternatives = new ArrayList<>();
        private List<String> synonyms = new ArrayList<>();
        private boolean isCorrect;
        private String timestamp; // ISO string from frontend
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportReviewSessionDto {
        private String id;
        private String filter;
        private int total;
        private int correct;
        private int incorrect;
        private int accuracy;
        private List<Map<String, Object>> words = new ArrayList<>();
        private String timestamp; // ISO string from frontend
    }
}
