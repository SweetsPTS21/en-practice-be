package com.swpts.enpracticebe.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "review_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String filter;

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private int correct;

    @Column(nullable = false)
    private int incorrect;

    @Column(nullable = false)
    private int accuracy;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> words = new ArrayList<>();

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @PrePersist
    protected void onCreate() {
        if (reviewedAt == null) {
            reviewedAt = Instant.now();
        }
    }

    @JsonProperty("timestamp")
    public Instant getTimestamp() {
        return reviewedAt;
    }
}
