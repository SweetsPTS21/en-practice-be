package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.response.dashboard.RecommendedPractice;
import com.swpts.enpracticebe.entity.IeltsTestAttempt;
import com.swpts.enpracticebe.repository.IeltsTestAttemptRepository;
import com.swpts.enpracticebe.service.UserStatsAggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserStatsAggregatorServiceImpl implements UserStatsAggregatorService {

    private final IeltsTestAttemptRepository ieltsTestAttemptRepository;

    @Override
    @Cacheable(value = "userWeakSkills", key = "#userId", unless = "#result == null or #result.isEmpty()")
    public List<String> getWeakSkills(UUID userId) {
        // Logic to determine weak skills based on recent IeltsTestAttempts or other data
        // For Phase 2 MVP, we'll return a calculated approximation or a default set if no data exists
        List<String> weakSkills = new ArrayList<>();
        
        List<IeltsTestAttempt> recentAttempts = ieltsTestAttemptRepository.findByUserIdOrderByStartedAtDesc(userId);
        
        if (recentAttempts != null && !recentAttempts.isEmpty()) {
            // Find patterns in low scores. Here we mock some weak skills based on general patterns
            // In a fully developed system, we would query user_skill_stats
            IeltsTestAttempt latest = recentAttempts.get(0);
            if (latest.getBandScore() != null && latest.getBandScore() < 6.5) {
                weakSkills.add("True/False/Not Given");
                weakSkills.add("Multiple Choice");
            }
        }
        
        // Add defaults if still empty
        if (weakSkills.isEmpty()) {
            weakSkills.add("Academic Vocabulary");
            weakSkills.add("Map Labeling");
        }
        
        return weakSkills;
    }

    @Override
    public List<RecommendedPractice> getRecommendedPractice(List<String> weakSkills) {
        List<RecommendedPractice> recommendations = new ArrayList<>();
        
        if (weakSkills.contains("True/False/Not Given") || weakSkills.contains("Multiple Choice")) {
            recommendations.add(RecommendedPractice.builder()
                .id("r1")
                .title("Reading Practice")
                .description("True / False / Not Given & Multiple Choice focuses")
                .type("READING")
                .difficulty("Medium")
                .estimatedTime("8 mins")
                .path("/ielts")
                .build());
        }
        
        if (weakSkills.contains("Academic Vocabulary") || weakSkills.contains("Map Labeling")) {
            recommendations.add(RecommendedPractice.builder()
                .id("r2")
                .title("Vocabulary Builder")
                .description("Academic Words Set 4")
                .type("VOCAB")
                .difficulty("Medium")
                .estimatedTime("5 mins")
                .path("/")
                .build());
        }
        
        // Default filler if the generated list is too small
        if (recommendations.isEmpty()) {
            recommendations.add(RecommendedPractice.builder()
                .id("r3")
                .title("General Listening Practice")
                .description("Section 1 & 2 Focus")
                .type("LISTENING")
                .difficulty("Easy")
                .estimatedTime("10 mins")
                .path("/ielts")
                .build());
        }

        return recommendations;
    }
}
