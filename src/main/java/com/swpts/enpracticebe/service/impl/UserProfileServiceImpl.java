package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.constant.LeaderboardPeriod;
import com.swpts.enpracticebe.dto.request.profile.UpdateUserProfileRequest;
import com.swpts.enpracticebe.dto.response.dashboard.BandProgress;
import com.swpts.enpracticebe.dto.response.dashboard.RecentActivity;
import com.swpts.enpracticebe.dto.response.dashboard.RecommendedPractice;
import com.swpts.enpracticebe.dto.response.dashboard.ScoreProgress;
import com.swpts.enpracticebe.dto.response.dashboard.TodayGoal;
import com.swpts.enpracticebe.dto.response.dashboard.VocabProgress;
import com.swpts.enpracticebe.dto.response.dictionary.DictionaryStatsResponse;
import com.swpts.enpracticebe.dto.response.leaderboard.LeaderboardSummaryResponse;
import com.swpts.enpracticebe.dto.response.profile.LevelInfoResponse;
import com.swpts.enpracticebe.dto.response.profile.UserProfileGoalsResponse;
import com.swpts.enpracticebe.dto.response.profile.UserProfileOverviewResponse;
import com.swpts.enpracticebe.dto.response.profile.UserProfileResponse;
import com.swpts.enpracticebe.dto.response.profile.UserProfileSummaryResponse;
import com.swpts.enpracticebe.dto.response.profile.UserProfileStreakResponse;
import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.entity.UserPracticeRecommendation;
import com.swpts.enpracticebe.entity.UserProfile;
import com.swpts.enpracticebe.entity.UserProfileSnapshot;
import com.swpts.enpracticebe.repository.UserPracticeRecommendationRepository;
import com.swpts.enpracticebe.repository.UserProfileRepository;
import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.service.LeaderboardService;
import com.swpts.enpracticebe.service.UserProfileService;
import com.swpts.enpracticebe.service.UserProfileSnapshotService;
import com.swpts.enpracticebe.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private static final int DEFAULT_DAILY_GOAL_MINUTES = 30;

    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserPracticeRecommendationRepository recommendationRepository;
    private final LeaderboardService leaderboardService;
    private final DashboardActivityCache dashboardActivityCache;
    private final UserProfileSnapshotService userProfileSnapshotService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileSummaryResponse getCurrentUserProfileSummary() {
        return buildProfileSummary(authUtil.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        return buildProfile(authUtil.getUserId());
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentUserProfile(UpdateUserProfileRequest request) {
        UUID userId = authUtil.getUserId();
        User user = getUser(userId);

        if (request.getDisplayName() != null) {
            String displayName = request.getDisplayName().trim();
            if (!StringUtils.hasText(displayName)) {
                throw new IllegalArgumentException("Display name must not be blank");
            }
            user.setDisplayName(displayName);
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(normalizeNullableText(request.getAvatarUrl()));
        }

        if (request.getBio() != null) {
            user.setBio(normalizeNullableText(request.getBio()));
        }

        userRepository.save(user);

        boolean hasProfileUpdates = request.getTargetIeltsBand() != null
                || request.getTargetExamDate() != null
                || request.getDailyGoalMinutes() != null
                || request.getWeeklyWordGoal() != null
                || request.getPreferredSkill() != null;

        if (hasProfileUpdates) {
            UserProfile userProfile = userProfileRepository.findById(userId)
                    .orElseGet(() -> defaultUserProfile(userId));

            if (request.getTargetIeltsBand() != null) {
                userProfile.setTargetIeltsBand(request.getTargetIeltsBand());
            }
            if (request.getTargetExamDate() != null) {
                userProfile.setTargetExamDate(request.getTargetExamDate());
            }
            if (request.getDailyGoalMinutes() != null) {
                userProfile.setDailyGoalMinutes(request.getDailyGoalMinutes());
            }
            if (request.getWeeklyWordGoal() != null) {
                userProfile.setWeeklyWordGoal(request.getWeeklyWordGoal());
            }
            if (request.getPreferredSkill() != null) {
                userProfile.setPreferredSkill(normalizeNullableText(request.getPreferredSkill()));
            }

            userProfileRepository.save(userProfile);
        }

        return buildProfile(userId);
    }

    private UserProfileSummaryResponse buildProfileSummary(UUID userId) {
        ProfileContext context = loadProfileContext(userId);

        return UserProfileSummaryResponse.builder()
                .id(context.user().getId())
                .email(context.user().getEmail())
                .displayName(context.user().getDisplayName())
                .avatarUrl(context.user().getAvatarUrl())
                .goals(buildGoals(context.userProfile()))
                .levelInfo(buildLevelInfo(context.snapshot()))
                .overallBand(context.snapshot().getOverallBand())
                .currentStreak(context.snapshot().getCurrentStreak())
                .weeklyXp(context.snapshot().getWeeklyXp())
                .build();
    }

    private UserProfileResponse buildProfile(UUID userId) {
        ProfileContext context = loadProfileContext(userId);

        return UserProfileResponse.builder()
                .id(context.user().getId())
                .email(context.user().getEmail())
                .displayName(context.user().getDisplayName())
                .avatarUrl(context.user().getAvatarUrl())
                .bio(context.user().getBio())
                .createdAt(context.user().getCreatedAt())
                .lastLoginAt(context.user().getLastLoginAt())
                .goals(buildGoals(context.userProfile()))
                .levelInfo(buildLevelInfo(context.snapshot()))
                .overview(UserProfileOverviewResponse.builder()
                        .weeklyXp(context.snapshot().getWeeklyXp())
                        .totalLessonsCompleted(context.snapshot().getTotalLessonsCompleted())
                        .totalWordsLearned(context.snapshot().getTotalWordsLearned())
                        .totalStudyMinutes(context.snapshot().getTotalStudyMinutes())
                        .wordsToReviewToday(context.snapshot().getWordsToReviewToday())
                        .currentStreak(context.snapshot().getCurrentStreak())
                        .longestStreak(context.snapshot().getLongestStreak())
                        .build())
                .streak(UserProfileStreakResponse.builder()
                        .currentStreak(context.snapshot().getCurrentStreak())
                        .longestStreak(context.snapshot().getLongestStreak())
                        .activeDaysLast30(context.snapshot().getActiveDaysLast30())
                        .heatmap(context.snapshot().getLast30DaysHeatmap())
                        .build())
                .todayGoal(TodayGoal.builder()
                        .targetMinutes(resolveDailyGoalMinutes(context.userProfile()))
                        .studiedMinutes(context.snapshot().getStudiedMinutesToday())
                        .percentage(calculatePercentage(
                                context.snapshot().getStudiedMinutesToday(),
                                resolveDailyGoalMinutes(context.userProfile())))
                        .build())
                .bandProgress(BandProgress.builder()
                        .listening(new ScoreProgress(context.snapshot().getListeningBand(), 0f))
                        .reading(new ScoreProgress(context.snapshot().getReadingBand(), 0f))
                        .speaking(new ScoreProgress(context.snapshot().getSpeakingBand(), 0f))
                        .writing(new ScoreProgress(context.snapshot().getWritingBand(), 0f))
                        .overall(new ScoreProgress(context.snapshot().getOverallBand(), 0f))
                        .build())
                .vocabProgress(VocabProgress.builder()
                        .totalWords(context.snapshot().getVocabTotalWords())
                        .masteredWords(context.snapshot().getVocabMasteredWords())
                        .reviewingWords(context.snapshot().getVocabReviewingWords())
                        .build())
                .dictionaryStats(DictionaryStatsResponse.builder()
                        .totalWords(context.snapshot().getVocabTotalWords())
                        .masteredWords(context.snapshot().getVocabMasteredWords())
                        .learningWords(context.snapshot().getLearningWords())
                        .wordsToReviewToday(context.snapshot().getWordsToReviewToday())
                        .favoriteWords(context.snapshot().getFavoriteWords())
                        .newWords(context.snapshot().getNewWords())
                        .build())
                .weakSkills(getWeakSkills(context.user().getId()))
                .recommendedPractice(getRecommendedPractice(context.user().getId()))
                .recentActivities(dashboardActivityCache.getRecentActivities(context.user().getId()))
                .leaderboardSummary(leaderboardService.getLeaderboardSummary(
                        context.user().getId(),
                        LeaderboardPeriod.WEEKLY))
                .build();
    }

    private ProfileContext loadProfileContext(UUID userId) {
        User user = getUser(userId);
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseGet(() -> defaultUserProfile(userId));
        UserProfileSnapshot snapshot = userProfileSnapshotService.getOrComputeSnapshot(userId);
        return new ProfileContext(user, userProfile, snapshot);
    }

    private UserProfileGoalsResponse buildGoals(UserProfile userProfile) {
        return UserProfileGoalsResponse.builder()
                .targetIeltsBand(userProfile.getTargetIeltsBand())
                .targetExamDate(userProfile.getTargetExamDate())
                .dailyGoalMinutes(resolveDailyGoalMinutes(userProfile))
                .weeklyWordGoal(userProfile.getWeeklyWordGoal())
                .preferredSkill(userProfile.getPreferredSkill())
                .build();
    }

    private LevelInfoResponse buildLevelInfo(UserProfileSnapshot snapshot) {
        return LevelInfoResponse.builder()
                .totalXp(snapshot.getTotalXp())
                .currentLevel(snapshot.getCurrentLevel())
                .currentLevelMinXp(snapshot.getCurrentLevelMinXp())
                .nextLevel(snapshot.getNextLevel())
                .nextLevelMinXp(snapshot.getNextLevelMinXp())
                .xpIntoCurrentLevel(snapshot.getXpIntoCurrentLevel())
                .xpNeededForNextLevel(snapshot.getXpNeededForNextLevel())
                .progressPercentage(snapshot.getLevelProgressPercentage())
                .build();
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private UserProfile defaultUserProfile(UUID userId) {
        return UserProfile.builder()
                .userId(userId)
                .dailyGoalMinutes(DEFAULT_DAILY_GOAL_MINUTES)
                .build();
    }

    private int resolveDailyGoalMinutes(UserProfile userProfile) {
        return userProfile.getDailyGoalMinutes() != null ? userProfile.getDailyGoalMinutes() : DEFAULT_DAILY_GOAL_MINUTES;
    }

    private String normalizeNullableText(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<String> getWeakSkills(UUID userId) {
        return recommendationRepository.findByUserId(userId)
                .map(UserPracticeRecommendation::getWeakSkills)
                .filter(list -> list != null && !list.isEmpty())
                .orElse(List.of("Academic Vocabulary", "Map Labeling"));
    }

    private List<RecommendedPractice> getRecommendedPractice(UUID userId) {
        return recommendationRepository.findByUserId(userId)
                .map(this::mapRecommendedPractice)
                .orElseGet(this::defaultRecommendations);
    }

    private List<RecommendedPractice> mapRecommendedPractice(UserPracticeRecommendation recommendation) {
        if (recommendation.getRecommendations() == null || recommendation.getRecommendations().isEmpty()) {
            return defaultRecommendations();
        }

        return recommendation.getRecommendations().stream()
                .map(item -> RecommendedPractice.builder()
                        .id(UUID.randomUUID().toString())
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .type(item.getType())
                        .difficulty(item.getDifficulty())
                        .estimatedTime(item.getEstimatedTime())
                        .path(item.getPath())
                        .reason(item.getReason())
                        .priority(item.getPriority())
                        .build())
                .toList();
    }

    private List<RecommendedPractice> defaultRecommendations() {
        return List.of(
                RecommendedPractice.builder()
                        .id("profile-r1")
                        .title("Diagnostic Test")
                        .description("Take a short Listening test to assess your baseline.")
                        .type("LISTENING")
                        .difficulty("Medium")
                        .estimatedTime("15 mins")
                        .path("/ielts")
                        .reason("We need a baseline to personalize your learning path.")
                        .priority(1)
                        .build()
        );
    }

    private int calculatePercentage(int studiedMinutes, int targetMinutes) {
        if (targetMinutes <= 0) {
            return 0;
        }
        return Math.min(100, (int) ((studiedMinutes * 100.0) / targetMinutes));
    }

    private record ProfileContext(User user, UserProfile userProfile, UserProfileSnapshot snapshot) {
    }
}
