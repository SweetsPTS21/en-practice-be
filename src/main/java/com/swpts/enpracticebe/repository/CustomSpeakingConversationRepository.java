package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.CustomSpeakingConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomSpeakingConversationRepository extends JpaRepository<CustomSpeakingConversation, UUID> {

    Page<CustomSpeakingConversation> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);

    Optional<CustomSpeakingConversation> findByIdAndUserId(UUID id, UUID userId);
}
