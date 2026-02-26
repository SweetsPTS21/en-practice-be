package com.swpts.enpracticebe.dto.response;

import com.swpts.enpracticebe.dto.FrequentlyWrongWord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {
    private long total;
    private long correct;
    private long incorrect;
    private int accuracy;

    @Builder.Default
    private List<FrequentlyWrongWord> frequentlyWrong = new ArrayList<>();

    @Builder.Default
    private List<Object> filtered = new ArrayList<>();
}
