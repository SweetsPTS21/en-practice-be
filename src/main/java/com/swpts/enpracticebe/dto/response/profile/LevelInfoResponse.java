package com.swpts.enpracticebe.dto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelInfoResponse {
    private int totalXp;
    private int currentLevel;
    private int currentLevelMinXp;
    private int nextLevel;
    private int nextLevelMinXp;
    private int xpIntoCurrentLevel;
    private int xpNeededForNextLevel;
    private int progressPercentage;
}
