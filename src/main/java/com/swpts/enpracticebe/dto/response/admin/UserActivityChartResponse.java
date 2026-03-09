package com.swpts.enpracticebe.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityChartResponse {
    private String date;
    private long activeUsers;
    private long attempts;
    private long vocabularyRecords;
}
