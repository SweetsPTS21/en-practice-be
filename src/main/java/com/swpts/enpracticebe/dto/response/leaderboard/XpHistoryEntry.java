package com.swpts.enpracticebe.dto.response.leaderboard;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant earnedAt;
}
