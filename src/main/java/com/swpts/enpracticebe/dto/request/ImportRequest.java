package com.swpts.enpracticebe.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportRequest {

    private List<ImportRecordDto> records = new ArrayList<>();
    private List<ImportReviewSessionDto> reviewSessions = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImportRecordDto {
        private String id;
        private String englishWord;
        private String userMeaning;
        private String correctMeaning;
        private List<String> alternatives = new ArrayList<>();
        private List<String> synonyms = new ArrayList<>();
        private Boolean isCorrect;
        private String timestamp; // ISO string from frontend
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImportReviewSessionDto {
        private String id;
        private String filter;
        private Integer total;
        private Integer correct;
        private Integer incorrect;
        private Integer accuracy;
        private List<Map<String, Object>> words = new ArrayList<>();
        private String timestamp; // ISO string from frontend
    }
}
