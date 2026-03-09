package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.response.dashboard.RecommendedPractice;

import java.util.List;
import java.util.UUID;

public interface UserStatsAggregatorService {
    List<String> getWeakSkills(UUID userId);
    List<RecommendedPractice> getRecommendedPractice(List<String> weakSkills);
}
