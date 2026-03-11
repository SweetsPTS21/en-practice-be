package com.swpts.enpracticebe.dto.response.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleSentence {
    private String sentence;
    private String translation;
}
