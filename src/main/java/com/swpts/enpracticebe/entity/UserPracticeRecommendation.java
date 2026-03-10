package com.swpts.enpracticebe.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_practice_recommendations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPracticeRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Type(JsonBinaryType.class)
    @Column(name = "weak_skills", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> weakSkills = new ArrayList<>();

    @Type(JsonBinaryType.class)
    @Column(name = "recommendations", columnDefinition = "jsonb")
    @Builder.Default
    private List<RecommendationItem> recommendations = new ArrayList<>();

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private String title;
        private String description;
        private String type;
        private String difficulty;
        private String estimatedTime;
        private String path;
        private String reason;
        private Integer priority;
    }
}
