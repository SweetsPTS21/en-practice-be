package com.swpts.enpracticebe.dto.request.dictionary;

import com.swpts.enpracticebe.dto.response.dictionary.ExampleSentence;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDictionaryWordRequest {

    @Size(max = 200, message = "Word must not exceed 200 characters")
    private String word;

    @Size(max = 200, message = "IPA must not exceed 200 characters")
    private String ipa;

    @Size(max = 50, message = "Word type must not exceed 50 characters")
    private String wordType;

    private String meaning;
    private String explanation;
    private String note;
    private List<ExampleSentence> examples;
    private List<String> tags;
    private Boolean isFavorite;
}
