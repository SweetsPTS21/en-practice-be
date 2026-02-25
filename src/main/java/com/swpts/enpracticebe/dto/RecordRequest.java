package com.swpts.enpracticebe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RecordRequest {

    @NotBlank
    private String englishWord;

    @NotBlank
    private String userMeaning;

    @NotBlank
    private String correctMeaning;

    private List<String> alternatives = new ArrayList<>();
    private List<String> synonyms = new ArrayList<>();
    private Boolean isCorrect;
}
