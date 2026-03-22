package com.swpts.enpracticebe.dto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileSummaryResponse {
    private UUID id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private UserProfileGoalsResponse goals;
    private LevelInfoResponse levelInfo;
    private Float overallBand;
    private Integer currentStreak;
    private Integer weeklyXp;
}
