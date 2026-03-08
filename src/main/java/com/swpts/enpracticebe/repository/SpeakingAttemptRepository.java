package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.SpeakingAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpeakingAttemptRepository extends JpaRepository<SpeakingAttempt, UUID> {

    List<SpeakingAttempt> findByUserIdOrderBySubmittedAtDesc(UUID userId);

    List<SpeakingAttempt> findByTopicIdAndUserIdOrderBySubmittedAtDesc(UUID topicId, UUID userId);
}
