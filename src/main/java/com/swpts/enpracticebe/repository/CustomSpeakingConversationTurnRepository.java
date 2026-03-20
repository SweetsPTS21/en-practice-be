package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.CustomSpeakingConversationTurn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomSpeakingConversationTurnRepository extends JpaRepository<CustomSpeakingConversationTurn, UUID> {

    List<CustomSpeakingConversationTurn> findByConversationIdOrderByTurnNumberAsc(UUID conversationId);
}
