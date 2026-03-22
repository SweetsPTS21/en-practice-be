package com.swpts.enpracticebe.dto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileOverviewResponse {
    private int weeklyXp;
    private long totalLessonsCompleted;
    private long totalWordsLearned;
    private int totalStudyMinutes;
    private long wordsToReviewToday;
    private int currentStreak;
    private int longestStreak;
}
