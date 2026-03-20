package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.constant.ActivityType;
import com.swpts.enpracticebe.constant.CustomConversationProperties;
import com.swpts.enpracticebe.dto.request.speaking.StartCustomConversationRequest;
import com.swpts.enpracticebe.dto.request.speaking.SubmitCustomConversationTurnRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.ai.AiAskResponse;
import com.swpts.enpracticebe.dto.response.speaking.CustomConversationResponse;
import com.swpts.enpracticebe.dto.response.speaking.CustomConversationStepResponse;
import com.swpts.enpracticebe.dto.response.speaking.CustomConversationTurnResponse;
import com.swpts.enpracticebe.dto.speech.SpeechAnalyticsDto;
import com.swpts.enpracticebe.entity.CustomSpeakingConversation;
import com.swpts.enpracticebe.entity.CustomSpeakingConversationTurn;
import com.swpts.enpracticebe.exception.ForbiddenException;
import com.swpts.enpracticebe.repository.CustomSpeakingConversationRepository;
import com.swpts.enpracticebe.repository.CustomSpeakingConversationTurnRepository;
import com.swpts.enpracticebe.service.CustomConversationSpeakingService;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.service.UserActivityLogService;
import com.swpts.enpracticebe.util.AuthUtil;
import com.swpts.enpracticebe.util.JsonUtil;
import com.swpts.enpracticebe.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomConversationSpeakingServiceImpl implements CustomConversationSpeakingService {
    private static final String DEFAULT_OPENING_MESSAGE =
            "Oh, this topic is interesting. I am curious, what is the first thing that comes to mind for you?";
    private static final String DEFAULT_REPLY_MESSAGE =
            "Hmm, that is interesting. What is the part of it that stands out most to you?";

    private final CustomSpeakingConversationRepository conversationRepository;
    private final CustomSpeakingConversationTurnRepository turnRepository;
    private final UserActivityLogService userActivityLogService;
    private final OpenClawService openClawService;
    private final CustomConversationGradingService gradingService;
    private final ObjectMapper objectMapper;
    private final AuthUtil authUtil;
    private final CustomConversationProperties properties;

    @Override
    @Transactional
    public CustomConversationStepResponse startConversation(StartCustomConversationRequest request, UUID userId) {
        String topic = request.getTopic().trim();
        String startPrompt = PromptBuilder.buildCustomConversationStartPrompt(
                topic,
                request.getStyle(),
                request.getPersonality(),
                request.getExpertise());

        CustomSpeakingConversation conversation = CustomSpeakingConversation.builder()
                .userId(userId)
                .title("Starting conversation")
                .topic(topic)
                .style(request.getStyle())
                .personality(request.getPersonality())
                .voiceName(request.getVoiceName())
                .expertise(request.getExpertise())
                .gradingEnabled(Boolean.TRUE.equals(request.getGradingEnabled()))
                .status(CustomSpeakingConversation.ConversationStatus.IN_PROGRESS)
                .maxUserTurns(properties.getMaxUserTurns())
                .userTurnCount(0)
                .totalTurns(0)
                .systemPromptSnapshot(startPrompt)
                .build();
        conversation = conversationRepository.save(conversation);

        StartPayload startPayload = generateStartPayload(startPrompt, topic, userId, conversation.getId());
        conversation.setTitle(startPayload.title());
        conversationRepository.save(conversation);

        CustomSpeakingConversationTurn firstTurn = CustomSpeakingConversationTurn.builder()
                .conversationId(conversation.getId())
                .turnNumber(1)
                .aiMessage(startPayload.openingMessage())
                .build();
        turnRepository.save(firstTurn);

        conversation.setTotalTurns(1);
        conversationRepository.save(conversation);

        userActivityLogService.logActivity(
                userId,
                ActivityType.CUSTOM_SPEAKING_CONVERSATION_ATTEMPT,
                conversation.getId(),
                conversation.getTitle());

        return buildStepResponse(conversation, firstTurn.getTurnNumber(), firstTurn.getAiMessage(), false);
    }

    @Override
    @Transactional
    public CustomConversationStepResponse submitTurn(UUID conversationId,
                                                     SubmitCustomConversationTurnRequest request,
                                                     UUID userId) {
        CustomSpeakingConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        if (conversation.getStatus() != CustomSpeakingConversation.ConversationStatus.IN_PROGRESS) {
            throw new NoSuchElementException("Conversation is not in progress");
        }

        List<CustomSpeakingConversationTurn> turns =
                turnRepository.findByConversationIdOrderByTurnNumberAsc(conversationId);

        CustomSpeakingConversationTurn currentTurn = turns.stream()
                .filter(t -> t.getUserTranscript() == null || t.getUserTranscript().isBlank())
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No pending turn found"));

        currentTurn.setUserTranscript(request.getTranscript());
        currentTurn.setAudioUrl(request.getAudioUrl());
        currentTurn.setTimeSpentSeconds(request.getTimeSpentSeconds());
        applySpeechAnalytics(currentTurn, request.getSpeechAnalytics());
        turnRepository.save(currentTurn);

        int nextUserTurnCount = conversation.getUserTurnCount() + 1;
        conversation.setUserTurnCount(nextUserTurnCount);

        if (nextUserTurnCount >= conversation.getMaxUserTurns()) {
            completeConversation(conversation);
            return buildStepResponse(conversation, currentTurn.getTurnNumber(), null, true);
        }

        String aiMessage = generateNextAiMessage(conversation, turns, request.getTranscript(), userId);
        int nextTurnNumber = currentTurn.getTurnNumber() + 1;

        CustomSpeakingConversationTurn nextTurn = CustomSpeakingConversationTurn.builder()
                .conversationId(conversationId)
                .turnNumber(nextTurnNumber)
                .aiMessage(aiMessage)
                .build();
        turnRepository.save(nextTurn);

        conversation.setTotalTurns(nextTurnNumber);
        conversationRepository.save(conversation);

        return buildStepResponse(conversation, nextTurnNumber, aiMessage, false);
    }

    @Override
    @Transactional
    public CustomConversationStepResponse finishConversation(UUID conversationId, UUID userId) {
        CustomSpeakingConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        if (conversation.getStatus() != CustomSpeakingConversation.ConversationStatus.IN_PROGRESS) {
            throw new NoSuchElementException("Conversation is not in progress");
        }

        completeConversation(conversation);
        return buildStepResponse(conversation, conversation.getTotalTurns(), null, true);
    }

    @Override
    public CustomConversationResponse getConversation(UUID conversationId, UUID userId) {
        CustomSpeakingConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found"));

        if (!Objects.equals(userId, conversation.getUserId()) && !authUtil.isAdmin()) {
            throw new ForbiddenException("Unauthorized: You can only access your own conversation");
        }

        List<CustomSpeakingConversationTurn> turns =
                turnRepository.findByConversationIdOrderByTurnNumberAsc(conversationId);

        return toConversationResponse(conversation, turns);
    }

    @Override
    public PageResponse<CustomConversationResponse> getConversationHistory(int page, int size, UUID userId) {
        UUID authUserId = authUtil.getUserId();

        if (!authUserId.equals(userId) && !authUtil.isAdmin()) {
            throw new ForbiddenException("Unauthorized: You can only access your own conversation history");
        }

        Page<CustomSpeakingConversation> convPage = conversationRepository
                .findByUserIdOrderByStartedAtDesc(userId, PageRequest.of(page, size));

        List<CustomConversationResponse> items = convPage.getContent().stream()
                .map(conv -> toConversationResponse(conv, null))
                .collect(Collectors.toList());

        return PageResponse.<CustomConversationResponse>builder()
                .items(items)
                .page(convPage.getNumber())
                .size(convPage.getSize())
                .totalElements(convPage.getTotalElements())
                .totalPages(convPage.getTotalPages())
                .build();
    }

    private StartPayload generateStartPayload(String prompt, String topic, UUID userId, UUID conversationId) {
        try {
            AiAskResponse aiResponse = openClawService.askFreestyleConversationAi(prompt, userId, conversationId);
            String raw = aiResponse.getAnswer().trim();
            try {
                JsonNode json = objectMapper.readTree(JsonUtil.extractJson(raw));

                String title = textOrFallback(json, "title", buildFallbackTitle(topic));
                String openingMessage = normalizeAiMessage(textOrFallback(
                        json,
                        "opening_message",
                        DEFAULT_OPENING_MESSAGE), DEFAULT_OPENING_MESSAGE);
                return new StartPayload(title, openingMessage);
            } catch (Exception parseException) {
                log.warn("Failed to parse custom conversation start payload JSON, using normalized raw text: {}",
                        parseException.getMessage());
                return new StartPayload(
                        buildFallbackTitle(topic),
                        normalizeAiMessage(raw, DEFAULT_OPENING_MESSAGE));
            }
        } catch (Exception e) {
            log.warn("Failed to generate custom conversation start payload, using fallback: {}", e.getMessage());
            return new StartPayload(
                    buildFallbackTitle(topic),
                    DEFAULT_OPENING_MESSAGE);
        }
    }

    private String generateNextAiMessage(CustomSpeakingConversation conversation,
                                         List<CustomSpeakingConversationTurn> turns,
                                         String latestTranscript,
                                         UUID userId) {
        String prompt = PromptBuilder.buildCustomConversationReplyPrompt(
                conversation.getTopic(),
                conversation.getStyle(),
                conversation.getPersonality(),
                conversation.getExpertise(),
                turns,
                latestTranscript,
                Math.max(conversation.getMaxUserTurns() - conversation.getUserTurnCount(), 0));
        try {
            AiAskResponse aiResponse = openClawService.askFreestyleConversationAi(
                    prompt,
                    userId,
                    conversation.getId());
            String raw = aiResponse.getAnswer().trim();
            try {
                JsonNode json = objectMapper.readTree(JsonUtil.extractJson(raw));
                return normalizeAiMessage(textOrFallback(json, "response", raw), DEFAULT_REPLY_MESSAGE);
            } catch (Exception parseException) {
                log.warn("Failed to parse custom conversation reply JSON, using normalized raw text: {}",
                        parseException.getMessage());
                return normalizeAiMessage(raw, DEFAULT_REPLY_MESSAGE);
            }
        } catch (Exception e) {
            log.warn("Failed to generate custom conversation reply, using fallback: {}", e.getMessage());
            return DEFAULT_REPLY_MESSAGE;
        }
    }

    private void completeConversation(CustomSpeakingConversation conversation) {
        conversation.setStatus(CustomSpeakingConversation.ConversationStatus.COMPLETED);
        conversation.setCompletedAt(Instant.now());

        List<CustomSpeakingConversationTurn> allTurns =
                turnRepository.findByConversationIdOrderByTurnNumberAsc(conversation.getId());
        int totalTime = allTurns.stream()
                .mapToInt(t -> t.getTimeSpentSeconds() != null ? t.getTimeSpentSeconds() : 0)
                .sum();
        conversation.setTimeSpentSeconds(totalTime);
        conversationRepository.save(conversation);

        if (Boolean.TRUE.equals(conversation.getGradingEnabled())) {
            final UUID convId = conversation.getId();
            final UUID ownerId = conversation.getUserId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    gradingService.gradeConversationAsync(convId, ownerId);
                }
            });
        }
    }

    private void applySpeechAnalytics(CustomSpeakingConversationTurn turn, SpeechAnalyticsDto analytics) {
        if (analytics == null) {
            return;
        }

        turn.setWordCount(analytics.getWordCount());
        turn.setWordsPerMinute(analytics.getWordsPerMinute());
        turn.setPauseCount(analytics.getPauseCount());
        turn.setAvgPauseDurationMs(analytics.getAvgPauseDurationMs());
        turn.setLongPauseCount(analytics.getLongPauseCount());
        turn.setFillerWordCount(analytics.getFillerWordCount());
        turn.setAvgWordConfidence(analytics.getAvgWordConfidence());
        try {
            turn.setSpeechDataJson(objectMapper.writeValueAsString(analytics));
        } catch (Exception e) {
            log.warn("Failed to serialize speech analytics for custom conversation turn: {}", e.getMessage());
        }
    }

    private CustomConversationResponse toConversationResponse(CustomSpeakingConversation conversation,
                                                              List<CustomSpeakingConversationTurn> turns) {
        List<CustomConversationTurnResponse> turnResponses = null;
        if (turns != null) {
            turnResponses = turns.stream()
                    .map(turn -> CustomConversationTurnResponse.builder()
                            .id(turn.getId())
                            .turnNumber(turn.getTurnNumber())
                            .aiMessage(turn.getAiMessage())
                            .userTranscript(turn.getUserTranscript())
                            .audioUrl(turn.getAudioUrl())
                            .timeSpentSeconds(turn.getTimeSpentSeconds())
                            .speechAnalytics(parseSpeechData(turn.getSpeechDataJson()))
                            .createdAt(turn.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
        }

        return CustomConversationResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .topic(conversation.getTopic())
                .style(conversation.getStyle())
                .personality(conversation.getPersonality())
                .voiceName(conversation.getVoiceName())
                .expertise(conversation.getExpertise())
                .gradingEnabled(conversation.getGradingEnabled())
                .status(conversation.getStatus().name())
                .maxUserTurns(conversation.getMaxUserTurns())
                .userTurnCount(conversation.getUserTurnCount())
                .totalTurns(conversation.getTotalTurns())
                .timeSpentSeconds(conversation.getTimeSpentSeconds())
                .fluencyScore(conversation.getFluencyScore())
                .vocabularyScore(conversation.getVocabularyScore())
                .coherenceScore(conversation.getCoherenceScore())
                .pronunciationScore(conversation.getPronunciationScore())
                .overallScore(conversation.getOverallScore())
                .aiFeedback(conversation.getAiFeedback())
                .startedAt(conversation.getStartedAt())
                .completedAt(conversation.getCompletedAt())
                .gradedAt(conversation.getGradedAt())
                .turns(turnResponses)
                .build();
    }

    private CustomConversationStepResponse buildStepResponse(CustomSpeakingConversation conversation,
                                                             Integer turnNumber,
                                                             String aiMessage,
                                                             boolean complete) {
        return CustomConversationStepResponse.builder()
                .conversationId(conversation.getId())
                .title(conversation.getTitle())
                .turnNumber(turnNumber)
                .aiMessage(aiMessage)
                .conversationComplete(complete)
                .gradingEnabled(Boolean.TRUE.equals(conversation.getGradingEnabled()))
                .status(conversation.getStatus().name())
                .userTurnCount(conversation.getUserTurnCount())
                .maxUserTurns(conversation.getMaxUserTurns())
                .voiceName(conversation.getVoiceName())
                .build();
    }

    private SpeechAnalyticsDto parseSpeechData(String speechDataJson) {
        if (speechDataJson == null || speechDataJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(speechDataJson, SpeechAnalyticsDto.class);
        } catch (Exception e) {
            log.warn("Failed to parse custom conversation speech analytics: {}", e.getMessage());
            return null;
        }
    }

    private String buildFallbackTitle(String topic) {
        if (topic == null || topic.isBlank()) {
            return "Free Conversation";
        }
        String normalized = topic.trim();
        return normalized.length() <= 40 ? normalized : normalized.substring(0, 40).trim() + "...";
    }

    private String textOrFallback(JsonNode node, String field, String fallback) {
        if (node != null && node.has(field)) {
            String value = node.get(field).asText();
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return fallback;
    }

    private String normalizeAiMessage(String message, String fallback) {
        if (message == null || message.isBlank()) {
            return fallback;
        }

        String normalized = message.trim()
                .replace("```json", " ")
                .replace("```", " ")
                .replaceAll("(?i)^(response|ai|assistant|speaker)\\s*[:\\-]\\s*", "")
                .replaceAll("\\s+([,.!?])", "$1")
                .replaceAll("\\s+", " ")
                .trim();

        normalized = stripWrappingQuotes(normalized);
        return normalized.isBlank() ? fallback : normalized;
    }

    private String stripWrappingQuotes(String message) {
        String normalized = message;
        while (normalized.length() >= 2) {
            char first = normalized.charAt(0);
            char last = normalized.charAt(normalized.length() - 1);
            boolean wrappedInDoubleQuotes = first == '"' && last == '"';
            boolean wrappedInSingleQuotes = first == '\'' && last == '\'';

            if (!wrappedInDoubleQuotes && !wrappedInSingleQuotes) {
                break;
            }

            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private record StartPayload(String title, String openingMessage) {
    }
}
