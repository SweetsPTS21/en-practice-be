package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.response.ai.AiAskResponse;
import com.swpts.enpracticebe.entity.SpeakingConversation;
import com.swpts.enpracticebe.entity.SpeakingConversationTurn;
import com.swpts.enpracticebe.entity.SpeakingTopic;
import com.swpts.enpracticebe.repository.SpeakingConversationRepository;
import com.swpts.enpracticebe.repository.SpeakingConversationTurnRepository;
import com.swpts.enpracticebe.repository.SpeakingTopicRepository;
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
public class ConversationGradingService {

    private final SpeakingConversationRepository conversationRepository;
    private final SpeakingConversationTurnRepository turnRepository;
    private final SpeakingTopicRepository topicRepository;
    private final OpenClawService openClawService;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper;

    @Async("aiGradingExecutor")
    public void gradeConversationAsync(UUID conversationId, UUID userId) {
        try {
            SpeakingConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

            SpeakingTopic topic = topicRepository.findById(conversation.getTopicId())
                    .orElseThrow(() -> new NoSuchElementException("Topic not found"));

            conversation.setStatus(SpeakingConversation.ConversationStatus.GRADING);
            conversationRepository.save(conversation);

            List<SpeakingConversationTurn> turns =
                    turnRepository.findByConversationIdOrderByTurnNumberAsc(conversationId);

            String gradingPrompt = PromptBuilder.buildConversationGradingPrompt(topic, turns);
            AiAskResponse aiResponse = openClawService.systemCallAi(gradingPrompt);
            // Pass already-fetched turns to avoid a duplicate DB query
            parseAndSaveResult(conversation, aiResponse.getAnswer(), turns);

            try {
                pushNotificationService.sendNotificationToUser(
                        userId,
                        "Speaking Conversation Graded",
                        "Your conversation has been graded. Band: " + conversation.getOverallBandScore());
            } catch (Exception e) {
                log.warn("Failed to send push notification: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error grading conversation {}: {}", conversationId, e.getMessage(), e);
            try {
                SpeakingConversation conv = conversationRepository.findById(conversationId).orElse(null);
                if (conv != null) {
                    conv.setStatus(SpeakingConversation.ConversationStatus.FAILED);
                    conv.setAiFeedback("AI grading failed: " + e.getMessage());
                    conversationRepository.save(conv);
                }
            } catch (Exception ex) {
                log.error("Failed to update conversation status to FAILED: {}", ex.getMessage());
            }
        }
    }


    private void parseAndSaveResult(SpeakingConversation conversation, String aiAnswer,
                                    List<SpeakingConversationTurn> turns) {
        try {
            String jsonStr = JsonUtil.extractJson(aiAnswer);
            JsonNode node = objectMapper.readTree(jsonStr);

            conversation.setFluencyScore(getFloatField(node, "fluency"));
            conversation.setLexicalScore(getFloatField(node, "lexical"));
            conversation.setGrammarScore(getFloatField(node, "grammar"));

            Float aiPronScore = getFloatField(node, "pronunciation");

            // ─── Hybrid pronunciation scoring from turns ──────────────────────
            OptionalDouble avgConfOpt = turns.stream()
                    .filter(t -> t.getAvgWordConfidence() != null && t.getAvgWordConfidence() > 0)
                    .mapToDouble(SpeakingConversationTurn::getAvgWordConfidence)
                    .average();

            if (aiPronScore != null && avgConfOpt.isPresent()) {
                float confidenceScore = (float) (avgConfOpt.getAsDouble() * 9.0);
                float hybridScore = (float) (0.6 * aiPronScore + 0.4 * confidenceScore);
                hybridScore = Math.round(hybridScore * 2) / 2.0f;
                hybridScore = Math.max(1.0f, Math.min(9.0f, hybridScore));
                conversation.setPronunciationScore(hybridScore);
            } else {
                conversation.setPronunciationScore(aiPronScore);
            }

            conversation.setOverallBandScore(getFloatField(node, "overall_band"));

            String feedback = node.has("feedback") ? node.get("feedback").asText() : aiAnswer;
            conversation.setAiFeedback(feedback);
            conversation.setStatus(SpeakingConversation.ConversationStatus.GRADED);
            conversation.setGradedAt(Instant.now());

        } catch (Exception e) {
            log.warn("Failed to parse AI grading JSON: {}", e.getMessage());
            conversation.setAiFeedback(aiAnswer);
            conversation.setStatus(SpeakingConversation.ConversationStatus.GRADED);
            conversation.setGradedAt(Instant.now());
        }

        conversationRepository.save(conversation);
    }

    private Float getFloatField(JsonNode node, String field) {
        return JsonUtil.getFloatField(node, field);
    }
}
