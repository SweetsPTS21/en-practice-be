package com.swpts.enpracticebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private StreakInfo streak;
    private TodayGoal todayGoal;
    private List<DailyTask> dailyTasks;
    private ProgressOverview progress;
    private List<RecentActivity> recentActivities;
    private List<QuickPracticeItem> quickPractice;
}
