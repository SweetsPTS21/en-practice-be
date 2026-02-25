package com.swpts.enpracticebe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCountsDto {
    private long today;
    private long week;
    private long month;
    private long wrong;
    private long all;
}
