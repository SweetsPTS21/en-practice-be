package com.swpts.enpracticebe.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalUsers;
    private long activeUsersToday;
    private long newUsersThisWeek;

    private ContentStats contentStats;
    private ActivityStats activityStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentStats {
        private ContentCount ieltsTests;
        private ContentCount speakingTopics;
        private ContentCount writingTasks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentCount {
        private long total;
        private long published;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityStats {
        private long totalAttempts;
        private long attemptsToday;
        private long vocabularyRecordsToday;
    }
}
