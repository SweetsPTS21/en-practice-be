package com.swpts.enpracticebe.dto.response.leaderboard;

import com.swpts.enpracticebe.constant.LeaderboardPeriod;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LeaderboardSummaryResponse {
    private LeaderboardPeriod period;
    private MyRankInfo myRank;
    private List<LeaderboardEntry> topThree;
}
