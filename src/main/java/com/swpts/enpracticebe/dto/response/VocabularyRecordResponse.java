package com.swpts.enpracticebe.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyRecordResponse {
    private UUID id;
    private String englishWord;
    private String userMeaning;
    private String correctMeaning;
    private List<String> alternatives = new ArrayList<>();
    private List<String> synonyms = new ArrayList<>();
    private Boolean isCorrect;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "Asia/Ho_Chi_Minh"
    )
    private Instant testedAt;
}
