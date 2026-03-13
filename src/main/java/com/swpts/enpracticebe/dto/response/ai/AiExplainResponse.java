package com.swpts.enpracticebe.dto.response.ai;

import com.swpts.enpracticebe.dto.response.dictionary.ExampleSentence;
import com.swpts.enpracticebe.entity.DictionarySourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiExplainResponse {
    private String word;
    private String ipa;
    private String wordType;
    private String meaning;
    private String explanation;
    private List<ExampleSentence> examples;
    private String sourceType;
}
