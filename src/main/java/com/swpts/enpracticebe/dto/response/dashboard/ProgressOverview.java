package com.swpts.enpracticebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressOverview {
    private BandProgress bandProgress;
    private VocabProgress vocabProgress;
}
