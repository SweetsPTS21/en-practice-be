package com.swpts.enpracticebe.dto.response.leaderboard;

import com.swpts.enpracticebe.constant.XpSource;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class XpHistoryEntry {
    private UUID id;
    private XpSource source;
    private String description;
    private int xp;
    private Instant earnedAt;
}
