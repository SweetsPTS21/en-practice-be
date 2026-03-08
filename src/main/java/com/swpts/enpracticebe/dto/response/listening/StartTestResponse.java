package com.swpts.enpracticebe.dto.response.listening;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartTestResponse {
    private UUID attemptId;
    private IeltsTestDetailResponse testDetail;
}
