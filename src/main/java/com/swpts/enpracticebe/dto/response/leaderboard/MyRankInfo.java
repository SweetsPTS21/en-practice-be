package com.swpts.enpracticebe.dto.response.leaderboard;

import com.swpts.enpracticebe.constant.RankChangeDirection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyRankInfo {
    private int rank;
    private int totalParticipants;
    private int xp;
    private int xpToNextRank;
    private int rankChange;
    private RankChangeDirection rankChangeDirection;
}
