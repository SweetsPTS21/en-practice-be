package com.swpts.enpracticebe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartEntry {
    private String name;
    private long correct;
    private long incorrect;
    private long total;
}
