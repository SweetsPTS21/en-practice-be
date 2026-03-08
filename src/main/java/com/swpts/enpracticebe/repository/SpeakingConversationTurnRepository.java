package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.SpeakingConversationTurn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpeakingConversationTurnRepository extends JpaRepository<SpeakingConversationTurn, UUID> {

    List<SpeakingConversationTurn> findByConversationIdOrderByTurnNumberAsc(UUID conversationId);

    int countByConversationId(UUID conversationId);
}
