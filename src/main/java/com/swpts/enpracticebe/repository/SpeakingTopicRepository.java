package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.SpeakingTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpeakingTopicRepository extends JpaRepository<SpeakingTopic, UUID> {

    // Admin filters
    Page<SpeakingTopic> findByPart(SpeakingTopic.Part part, Pageable pageable);
    Page<SpeakingTopic> findByDifficulty(SpeakingTopic.Difficulty difficulty, Pageable pageable);
    Page<SpeakingTopic> findByIsPublished(Boolean isPublished, Pageable pageable);
    Page<SpeakingTopic> findByPartAndDifficulty(SpeakingTopic.Part part, SpeakingTopic.Difficulty difficulty, Pageable pageable);
    Page<SpeakingTopic> findByPartAndIsPublished(SpeakingTopic.Part part, Boolean isPublished, Pageable pageable);
    Page<SpeakingTopic> findByDifficultyAndIsPublished(SpeakingTopic.Difficulty difficulty, Boolean isPublished, Pageable pageable);
    Page<SpeakingTopic> findByPartAndDifficultyAndIsPublished(SpeakingTopic.Part part, SpeakingTopic.Difficulty difficulty, Boolean isPublished, Pageable pageable);

    // User-facing (published only)
    Page<SpeakingTopic> findByIsPublishedTrue(Pageable pageable);
    Page<SpeakingTopic> findByPartAndIsPublishedTrue(SpeakingTopic.Part part, Pageable pageable);
    Page<SpeakingTopic> findByDifficultyAndIsPublishedTrue(SpeakingTopic.Difficulty difficulty, Pageable pageable);
    Page<SpeakingTopic> findByPartAndDifficultyAndIsPublishedTrue(SpeakingTopic.Part part, SpeakingTopic.Difficulty difficulty, Pageable pageable);

    long countByIsPublishedTrue();
}
