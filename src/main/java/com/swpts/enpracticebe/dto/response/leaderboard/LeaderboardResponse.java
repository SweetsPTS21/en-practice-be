package com.swpts.enpracticebe.dto.response.leaderboard;

import com.swpts.enpracticebe.dto.response.PageResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LeaderboardResponse {
    private MyRankInfo myRank;
    private List<LeaderboardEntry> topUsers;
    private PageResponse<?> page;
}
