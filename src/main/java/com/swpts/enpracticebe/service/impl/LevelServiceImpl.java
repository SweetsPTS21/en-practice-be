package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.response.profile.LevelInfoResponse;
import com.swpts.enpracticebe.service.LevelService;
import org.springframework.stereotype.Service;

@Service
public class LevelServiceImpl implements LevelService {

    @Override
    public LevelInfoResponse calculateLevelInfo(Integer totalXp) {
        int safeTotalXp = Math.max(0, totalXp != null ? totalXp : 0);
        int currentLevel = 1;

        while (requiredXpForLevel(currentLevel + 1) <= safeTotalXp) {
            currentLevel++;
        }

        int currentLevelMinXp = requiredXpForLevel(currentLevel);
        int nextLevel = currentLevel + 1;
        int nextLevelMinXp = requiredXpForLevel(nextLevel);
        int xpIntoCurrentLevel = safeTotalXp - currentLevelMinXp;
        int xpNeededForNextLevel = Math.max(0, nextLevelMinXp - safeTotalXp);
        int levelSpan = Math.max(1, nextLevelMinXp - currentLevelMinXp);
        int progressPercentage = Math.min(100, (int) Math.round((xpIntoCurrentLevel * 100.0) / levelSpan));

        return LevelInfoResponse.builder()
                .totalXp(safeTotalXp)
                .currentLevel(currentLevel)
                .currentLevelMinXp(currentLevelMinXp)
                .nextLevel(nextLevel)
                .nextLevelMinXp(nextLevelMinXp)
                .xpIntoCurrentLevel(xpIntoCurrentLevel)
                .xpNeededForNextLevel(xpNeededForNextLevel)
                .progressPercentage(progressPercentage)
                .build();
    }

    private int requiredXpForLevel(int level) {
        if (level <= 1) {
            return 0;
        }

        long requiredXp = 50L * ((long) level * (level + 1) / 2 - 1);
        return requiredXp > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) requiredXp;
    }
}
