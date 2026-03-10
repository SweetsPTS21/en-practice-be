package com.swpts.enpracticebe.dto.response.leaderboard;

import com.swpts.enpracticebe.dto.response.PageResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class XpHistoryResponse {
    private int totalXP;
    private int weeklyXP;
    private List<XpHistoryEntry> history;
    private PageResponse<?> page;
}
