package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.response.AiAskResponse;
import com.swpts.enpracticebe.entity.SpeakingConversation;
import com.swpts.enpracticebe.entity.SpeakingConversationTurn;
import com.swpts.enpracticebe.entity.SpeakingTopic;
import com.swpts.enpracticebe.repository.SpeakingConversationRepository;
import com.swpts.enpracticebe.repository.SpeakingConversationTurnRepository;
import com.swpts.enpracticebe.repository.SpeakingTopicRepository;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.service.PushNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ConversationGradingService {

    private final SpeakingConversationRepository conversationRepository;
    private final SpeakingConversationTurnRepository turnRepository;
    private final SpeakingTopicRepository topicRepository;
    private final OpenClawService openClawService;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper;

    public ConversationGradingService(SpeakingConversationRepository conversationRepository,
                                      SpeakingConversationTurnRepository turnRepository,
                                      SpeakingTopicRepository topicRepository,
                                      OpenClawService openClawService,
                                      PushNotificationService pushNotificationService,
                                      ObjectMapper objectMapper) {
        this.conversationRepository = conversationRepository;
        this.turnRepository = turnRepository;
        this.topicRepository = topicRepository;
        this.openClawService = openClawService;
        this.pushNotificationService = pushNotificationService;
        this.objectMapper = objectMapper;
    }

    @Async("aiGradingExecutor")
    public void gradeConversationAsync(UUID conversationId, UUID userId) {
        try {
            SpeakingConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));

            SpeakingTopic topic = topicRepository.findById(conversation.getTopicId())
                    .orElseThrow(() -> new RuntimeException("Topic not found"));

            conversation.setStatus(SpeakingConversation.ConversationStatus.GRADING);
            conversationRepository.save(conversation);

            List<SpeakingConversationTurn> turns =
                    turnRepository.findByConversationIdOrderByTurnNumberAsc(conversationId);

            String gradingPrompt = buildGradingPrompt(topic, turns);
            AiAskResponse aiResponse = openClawService.askAi(gradingPrompt, userId);
            parseAndSaveResult(conversation, aiResponse.getAnswer());

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

    private String buildGradingPrompt(SpeakingTopic topic, List<SpeakingConversationTurn> turns) {
        StringBuilder transcript = new StringBuilder();
        for (SpeakingConversationTurn turn : turns) {
            transcript.append("Examiner: ").append(turn.getAiQuestion()).append("\n");
            if (turn.getUserTranscript() != null) {
                transcript.append("Student: ").append(turn.getUserTranscript()).append("\n");
            }
            transcript.append("\n");
        }

        String partDesc = switch (topic.getPart()) {
            case PART_1 -> "IELTS Speaking Part 1 (familiar topics, short answers)";
            case PART_2 -> "IELTS Speaking Part 2 (individual long turn / cue card)";
            case PART_3 -> "IELTS Speaking Part 3 (discussion, abstract ideas)";
        };

        return String.format("""
                You are an IELTS Speaking examiner. Grade the following %s conversation.
                
                **Topic:** %s
                
                **Full Conversation Transcript:**
                %s
                
                Grade on these 4 criteria (each 0.0 to 9.0, in 0.5 increments):
                1. Fluency and Coherence
                2. Lexical Resource
                3. Grammatical Range and Accuracy
                4. Pronunciation
                
                Also provide detailed feedback covering:
                - Strengths in the conversation
                - Areas for improvement
                - Specific examples from the transcript
                - Tips for achieving a higher band score
                
                You MUST respond in the following JSON format only, no extra text:
                {
                  "fluency": 6.5,
                  "lexical": 6.0,
                  "grammar": 6.5,
                  "pronunciation": 6.0,
                  "overall_band": 6.5,
                  "feedback": "Your detailed feedback in markdown format here..."
                }
                """,
                partDesc,
                topic.getQuestion(),
                transcript.toString());
    }

    private void parseAndSaveResult(SpeakingConversation conversation, String aiAnswer) {
        try {
            String jsonStr = extractJson(aiAnswer);
            JsonNode node = objectMapper.readTree(jsonStr);

            conversation.setFluencyScore(getFloatField(node, "fluency"));
            conversation.setLexicalScore(getFloatField(node, "lexical"));
            conversation.setGrammarScore(getFloatField(node, "grammar"));
            conversation.setPronunciationScore(getFloatField(node, "pronunciation"));
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

    private String extractJson(String text) {
        if (text.contains("```json")) {
            int start = text.indexOf("```json") + 7;
            int end = text.indexOf("```", start);
            if (end > start) return text.substring(start, end).trim();
        }
        if (text.contains("```")) {
            int start = text.indexOf("```") + 3;
            int end = text.indexOf("```", start);
            if (end > start) return text.substring(start, end).trim();
        }
        int braceStart = text.indexOf('{');
        int braceEnd = text.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return text.substring(braceStart, braceEnd + 1);
        }
        return text;
    }

    private Float getFloatField(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return (float) node.get(field).asDouble();
        }
        return null;
    }
}
