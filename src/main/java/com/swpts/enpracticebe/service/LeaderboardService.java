package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.constant.LeaderboardPeriod;
import com.swpts.enpracticebe.constant.LeaderboardScope;
import com.swpts.enpracticebe.dto.response.leaderboard.LeaderboardResponse;
import com.swpts.enpracticebe.dto.response.leaderboard.LeaderboardSummaryResponse;

import java.util.UUID;

public interface LeaderboardService {

    LeaderboardResponse getLeaderboard(UUID userId, LeaderboardPeriod period, LeaderboardScope scope, Float targetBand, int page, int size);

    LeaderboardSummaryResponse getLeaderboardSummary(UUID userId, LeaderboardPeriod period);

    void computeAndSnapshotRanks(LeaderboardPeriod period);
}
