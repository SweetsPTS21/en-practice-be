package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.response.dashboard.RecommendedPractice;
import com.swpts.enpracticebe.dto.response.dashboard.UserPerformanceProfile;
import java.util.List;
import java.util.UUID;

public interface UserStatsAggregatorService {
    List<String> getWeakSkills(UUID userId);
    List<String> getWeakSkillsFromProfile(UserPerformanceProfile profile);

    List<RecommendedPractice> getRecommendedPractice(UUID userId);
    List<RecommendedPractice> getRecommendedPracticeFromProfile(UserPerformanceProfile profile, UUID userId);

    UserPerformanceProfile buildPerformanceProfile(UUID userId);
}
