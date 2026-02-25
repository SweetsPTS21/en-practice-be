package com.swpts.enpracticebe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequentlyWrongWord {
    private String word;
    private String correctMeaning;
    private long wrongCount;
    private Instant lastAttempt;
}
