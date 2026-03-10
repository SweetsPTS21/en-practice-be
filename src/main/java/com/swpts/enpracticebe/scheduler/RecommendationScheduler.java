package com.swpts.enpracticebe.scheduler;

import com.swpts.enpracticebe.dto.response.dashboard.RecommendedPractice;
import com.swpts.enpracticebe.dto.response.dashboard.UserPerformanceProfile;
import com.swpts.enpracticebe.entity.UserPracticeRecommendation;
import com.swpts.enpracticebe.repository.UserPracticeRecommendationRepository;
import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.service.UserStatsAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationScheduler {

    private final UserRepository userRepository;
    private final UserStatsAggregatorService userStatsAggregatorService;
    private final UserPracticeRecommendationRepository recommendationRepository;

    /**
     * Runs every 6 hours to pre-compute AI recommendations for all users.
     * This prevents the slow AI call from blocking the Dashboard API.
     */
    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void computeRecommendationsForAllUsers() {
        log.info("Starting scheduled recommendation computation...");

        List<UUID> activeUserIds = userRepository.findAllActiveUserIds();

        int success = 0;
        int failed = 0;
        for (UUID userId : activeUserIds) {
            try {
                computeForUser(userId);
                success++;
            } catch (Exception e) {
                failed++;
                log.warn("Failed to compute recommendations for user {}: {}", userId, e.getMessage());
            }
        }
        log.info("Recommendation computation completed. Success: {}, Failed: {}", success, failed);
    }

    /**
     * Compute and persist recommendations for a single user.
     * Can also be called on-demand (e.g., after a user finishes a test).
     */
    @Transactional
    public void computeForUser(UUID userId) {
        UserPerformanceProfile profile = userStatsAggregatorService.buildPerformanceProfile(userId);
        List<String> weakSkills = userStatsAggregatorService.getWeakSkillsFromProfile(profile);
        List<RecommendedPractice> recommendations = userStatsAggregatorService.getRecommendedPracticeFromProfile(profile, userId);

        List<UserPracticeRecommendation.RecommendationItem> items = recommendations.stream()
                .map(r -> UserPracticeRecommendation.RecommendationItem.builder()
                        .title(r.getTitle())
                        .description(r.getDescription())
                        .type(r.getType())
                        .difficulty(r.getDifficulty())
                        .estimatedTime(r.getEstimatedTime())
                        .path(r.getPath())
                        .reason(r.getReason())
                        .priority(r.getPriority())
                        .build())
                .toList();

        UserPracticeRecommendation entity = recommendationRepository.findByUserId(userId)
                .orElse(UserPracticeRecommendation.builder().userId(userId).build());

        entity.setWeakSkills(weakSkills);
        entity.setRecommendations(items);
        entity.setComputedAt(Instant.now());

        recommendationRepository.save(entity);
    }
}
