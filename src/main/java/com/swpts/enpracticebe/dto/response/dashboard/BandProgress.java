package com.swpts.enpracticebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BandProgress {
    private ScoreProgress listening;
    private ScoreProgress reading;
    private ScoreProgress writing;
    private ScoreProgress speaking;
    private ScoreProgress overall;
}
