package com.swpts.enpracticebe.dto.response.leaderboard;

import com.swpts.enpracticebe.constant.RankChangeDirection;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LeaderboardEntry {
    private int rank;
    private UUID userId;
    private String displayName;
    private String avatarUrl;
    private Float targetBand;
    private int xp;
    private int currentStreak;
    private int rankChange;
    private RankChangeDirection rankChangeDirection;
}
