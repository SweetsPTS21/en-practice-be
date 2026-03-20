package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.response.ai.AiAskResponse;
import com.swpts.enpracticebe.entity.CustomSpeakingConversation;
import com.swpts.enpracticebe.entity.CustomSpeakingConversationTurn;
import com.swpts.enpracticebe.repository.CustomSpeakingConversationRepository;
import com.swpts.enpracticebe.repository.CustomSpeakingConversationTurnRepository;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.service.PushNotificationService;
import com.swpts.enpracticebe.util.JsonUtil;
import com.swpts.enpracticebe.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomConversationGradingService {

    private final CustomSpeakingConversationRepository conversationRepository;
    private final CustomSpeakingConversationTurnRepository turnRepository;
    private final OpenClawService openClawService;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper;

    @Async("aiGradingExecutor")
    public void gradeConversationAsync(UUID conversationId, UUID userId) {
        try {
            CustomSpeakingConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

            conversation.setStatus(CustomSpeakingConversation.ConversationStatus.GRADING);
            conversationRepository.save(conversation);

            List<CustomSpeakingConversationTurn> turns =
                    turnRepository.findByConversationIdOrderByTurnNumberAsc(conversationId);

            String gradingPrompt = PromptBuilder.buildCustomConversationGradingPrompt(
                    conversation.getTopic(),
                    conversation.getTitle(),
                    turns);
            AiAskResponse aiResponse = openClawService.systemCallAi(gradingPrompt);
            parseAndSaveResult(conversation, aiResponse.getAnswer(), turns);

            try {
                pushNotificationService.sendNotificationToUser(
                        userId,
                        "Conversation feedback is ready",
                        "Your custom speaking conversation has been graded.");
            } catch (Exception e) {
                log.warn("Failed to send push notification: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error grading custom conversation {}: {}", conversationId, e.getMessage(), e);
            try {
                CustomSpeakingConversation conv = conversationRepository.findById(conversationId).orElse(null);
                if (conv != null) {
                    conv.setStatus(CustomSpeakingConversation.ConversationStatus.FAILED);
                    conv.setAiFeedback("AI grading failed: " + e.getMessage());
                    conversationRepository.save(conv);
                }
            } catch (Exception ex) {
                log.error("Failed to update custom conversation status to FAILED: {}", ex.getMessage());
            }
        }
    }

    private void parseAndSaveResult(CustomSpeakingConversation conversation,
                                    String aiAnswer,
                                    List<CustomSpeakingConversationTurn> turns) {
        try {
            String jsonStr = JsonUtil.extractJson(aiAnswer);
            JsonNode node = objectMapper.readTree(jsonStr);

            conversation.setFluencyScore(getFloatField(node, "fluency"));
            conversation.setVocabularyScore(getFloatField(node, "vocabulary"));
            conversation.setCoherenceScore(getFloatField(node, "coherence"));

            Float aiPronScore = getFloatField(node, "pronunciation");
            OptionalDouble avgConfOpt = turns.stream()
                    .filter(t -> t.getAvgWordConfidence() != null && t.getAvgWordConfidence() > 0)
                    .mapToDouble(CustomSpeakingConversationTurn::getAvgWordConfidence)
                    .average();

            if (aiPronScore != null && avgConfOpt.isPresent()) {
                float confidenceScore = (float) (avgConfOpt.getAsDouble() * 10.0);
                float hybridScore = (float) (0.6 * aiPronScore + 0.4 * confidenceScore);
                hybridScore = Math.round(hybridScore * 2) / 2.0f;
                hybridScore = Math.max(0.0f, Math.min(10.0f, hybridScore));
                conversation.setPronunciationScore(hybridScore);
            } else {
                conversation.setPronunciationScore(aiPronScore);
            }

            conversation.setOverallScore(getFloatField(node, "overall_score"));
            conversation.setAiFeedback(node.has("feedback") ? node.get("feedback").asText() : aiAnswer);
            conversation.setStatus(CustomSpeakingConversation.ConversationStatus.GRADED);
            conversation.setGradedAt(Instant.now());
        } catch (Exception e) {
            log.warn("Failed to parse custom conversation grading JSON: {}", e.getMessage());
            conversation.setAiFeedback(aiAnswer);
            conversation.setStatus(CustomSpeakingConversation.ConversationStatus.GRADED);
            conversation.setGradedAt(Instant.now());
        }

        conversationRepository.save(conversation);
    }

    private Float getFloatField(JsonNode node, String field) {
        return JsonUtil.getFloatField(node, field);
    }
}
