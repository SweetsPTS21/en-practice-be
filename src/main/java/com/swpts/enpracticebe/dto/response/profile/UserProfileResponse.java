package com.swpts.enpracticebe.dto.response.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swpts.enpracticebe.dto.response.dashboard.BandProgress;
import com.swpts.enpracticebe.dto.response.dashboard.RecentActivity;
import com.swpts.enpracticebe.dto.response.dashboard.RecommendedPractice;
import com.swpts.enpracticebe.dto.response.dashboard.TodayGoal;
import com.swpts.enpracticebe.dto.response.dashboard.VocabProgress;
import com.swpts.enpracticebe.dto.response.dictionary.DictionaryStatsResponse;
import com.swpts.enpracticebe.dto.response.leaderboard.LeaderboardSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String bio;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant lastLoginAt;
    private UserProfileGoalsResponse goals;
    private LevelInfoResponse levelInfo;
    private UserProfileOverviewResponse overview;
    private UserProfileStreakResponse streak;
    private TodayGoal todayGoal;
    private BandProgress bandProgress;
    private VocabProgress vocabProgress;
    private DictionaryStatsResponse dictionaryStats;
    private List<String> weakSkills;
    private List<RecommendedPractice> recommendedPractice;
    private List<RecentActivity> recentActivities;
    private LeaderboardSummaryResponse leaderboardSummary;
}
