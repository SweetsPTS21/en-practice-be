package com.swpts.enpracticebe.dto.request.dictionary;

import com.swpts.enpracticebe.dto.response.dictionary.ExampleSentence;
import com.swpts.enpracticebe.entity.DictionarySourceType;
import jakarta.validation.constraints.NotBlank;
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
public class AddDictionaryWordRequest {

    @NotBlank(message = "Word is required")
    @Size(max = 200, message = "Word must not exceed 200 characters")
    private String word;

    @Size(max = 200, message = "IPA must not exceed 200 characters")
    private String ipa;

    @Size(max = 50, message = "Word type must not exceed 50 characters")
    private String wordType;

    @NotBlank(message = "Meaning is required")
    private String meaning;

    private String explanation;
    private String note;
    private List<ExampleSentence> examples;
    private List<String> tags;
    private DictionarySourceType sourceType;
    private Boolean isFavorite;
}
