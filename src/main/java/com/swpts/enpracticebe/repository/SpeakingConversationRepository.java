package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.SpeakingConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpeakingConversationRepository extends JpaRepository<SpeakingConversation, UUID> {

    Page<SpeakingConversation> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);

    Optional<SpeakingConversation> findByIdAndUserId(UUID id, UUID userId);
}
