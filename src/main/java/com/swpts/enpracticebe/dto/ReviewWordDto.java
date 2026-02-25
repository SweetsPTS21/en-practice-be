package com.swpts.enpracticebe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewWordDto {
    private String englishWord;
    private String correctMeaning;
    private List<String> alternatives;
}
