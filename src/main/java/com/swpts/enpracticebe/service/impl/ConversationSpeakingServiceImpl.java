package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.constant.ActivityType;
import com.swpts.enpracticebe.constant.TurnType;
import com.swpts.enpracticebe.dto.request.speaking.SubmitTurnRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.ai.AiAskResponse;
import com.swpts.enpracticebe.dto.response.speaking.ConversationResponse;
import com.swpts.enpracticebe.dto.response.speaking.NextQuestionResponse;
import com.swpts.enpracticebe.dto.speech.SpeechAnalyticsDto;
import com.swpts.enpracticebe.entity.SpeakingConversation;
import com.swpts.enpracticebe.entity.SpeakingConversationTurn;
import com.swpts.enpracticebe.entity.SpeakingTopic;
import com.swpts.enpracticebe.exception.ForbiddenException;
import com.swpts.enpracticebe.mapper.SpeakingMapper;
import com.swpts.enpracticebe.repository.SpeakingConversationRepository;
import com.swpts.enpracticebe.repository.SpeakingConversationTurnRepository;
import com.swpts.enpracticebe.repository.SpeakingTopicRepository;
import com.swpts.enpracticebe.service.ConversationSpeakingService;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.service.UserActivityLogService;
import com.swpts.enpracticebe.util.AuthUtil;
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
public class ConversationSpeakingServiceImpl implements ConversationSpeakingService {

    private final SpeakingConversationRepository conversationRepository;
    private final SpeakingConversationTurnRepository turnRepository;
    private final SpeakingTopicRepository topicRepository;
    private final UserActivityLogService userActivityLogService;
    private final OpenClawService openClawService;
    private final ConversationGradingService gradingService;
    private final SpeakingMapper speakingMapper;
    private final ObjectMapper objectMapper;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    public NextQuestionResponse startConversation(UUID topicId, UUID userId) {
        SpeakingTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic not found: " + topicId));

        if (!topic.getIsPublished()) {
            throw new NoSuchElementException("Topic is not published");
        }

        SpeakingConversation conversation = SpeakingConversation.builder()
                .userId(userId)
                .topicId(topicId)
                .status(SpeakingConversation.ConversationStatus.IN_PROGRESS)
                .totalTurns(0)
                .build();
        conversation = conversationRepository.save(conversation);

        SpeakingConversationTurn firstTurn = SpeakingConversationTurn.builder()
                .conversationId(conversation.getId())
                .turnNumber(1)
                .aiQuestion(topic.getQuestion())
                .turnType(TurnType.QUESTION.name())
                .followUpIndex(0)
                .build();
        turnRepository.save(firstTurn);

        conversation.setTotalTurns(1);
        conversationRepository.save(conversation);

        userActivityLogService.logActivity(userId, ActivityType.SPEAKING_CONVERSATION_ATTEMPT, conversation.getId(), topic.getQuestion());

        int totalExpectedQuestions = 1 + (topic.getFollowUpQuestions() != null ? topic.getFollowUpQuestions().size() : 0);

        return NextQuestionResponse.builder()
                .conversationId(conversation.getId())
                .turnNumber(1)
                .aiQuestion(topic.getQuestion())
                .turnType(TurnType.QUESTION.name())
                .lastTurn(totalExpectedQuestions <= 1)
                .conversationComplete(false)
                .build();
    }

    @Override
    @Transactional
    public NextQuestionResponse submitTurn(UUID conversationId, SubmitTurnRequest request, UUID userId) {
        SpeakingConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        if (conversation.getStatus() != SpeakingConversation.ConversationStatus.IN_PROGRESS) {
            throw new NoSuchElementException("Conversation is not in progress");
        }

        SpeakingTopic topic = topicRepository.findById(conversation.getTopicId())
                .orElseThrow(() -> new NoSuchElementException("Topic not found"));

        List<SpeakingConversationTurn> existingTurns =
                turnRepository.findByConversationIdOrderByTurnNumberAsc(conversationId);

        // Find the current unanswered turn and save user's response
        SpeakingConversationTurn currentTurn = existingTurns.stream()
                .filter(t -> t.getUserTranscript() == null || t.getUserTranscript().isBlank())
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No pending turn found"));

        currentTurn.setUserTranscript(request.getTranscript());
        currentTurn.setAudioUrl(request.getAudioUrl());
        currentTurn.setTimeSpentSeconds(request.getTimeSpentSeconds());

        // ─── Persist speech analytics if provided ─────────────────────────────
        SpeechAnalyticsDto analytics = request.getSpeechAnalytics();
        if (analytics != null) {
            currentTurn.setWordCount(analytics.getWordCount());
            currentTurn.setWordsPerMinute(analytics.getWordsPerMinute());
            currentTurn.setPauseCount(analytics.getPauseCount());
            currentTurn.setAvgPauseDurationMs(analytics.getAvgPauseDurationMs());
            currentTurn.setLongPauseCount(analytics.getLongPauseCount());
            currentTurn.setFillerWordCount(analytics.getFillerWordCount());
            currentTurn.setAvgWordConfidence(analytics.getAvgWordConfidence());
            try {
                currentTurn.setSpeechDataJson(objectMapper.writeValueAsString(analytics));
            } catch (Exception e) {
                log.warn("Failed to serialize speech analytics for turn: {}", e.getMessage());
            }
        }

        turnRepository.save(currentTurn);

        // Progress tracking: count only answered QUESTION turns
        List<String> followUps = topic.getFollowUpQuestions();
        int totalExpectedQuestions = 1 + (followUps != null ? followUps.size() : 0);

        long answeredQuestionCount = existingTurns.stream()
                .filter(t -> TurnType.QUESTION.name().equals(t.getTurnType())
                        && t.getUserTranscript() != null
                        && !t.getUserTranscript().isBlank())
                .count();

        // Current follow-up index from the last QUESTION turn
        int currentFollowUpIndex = existingTurns.stream()
                .filter(t -> TurnType.QUESTION.name().equals(t.getTurnType()))
                .mapToInt(t -> t.getFollowUpIndex() != null ? t.getFollowUpIndex() : 0)
                .max()
                .orElse(0);

        // All questions answered -> complete conversation
        if (answeredQuestionCount >= totalExpectedQuestions) {
            conversation.setStatus(SpeakingConversation.ConversationStatus.COMPLETED);
            conversation.setCompletedAt(Instant.now());

            int totalTime = existingTurns.stream()
                    .mapToInt(t -> t.getTimeSpentSeconds() != null ? t.getTimeSpentSeconds() : 0)
                    .sum();
            conversation.setTimeSpentSeconds(totalTime);
            conversationRepository.save(conversation);

            final UUID convId = conversation.getId();
            final UUID uid = userId;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    gradingService.gradeConversationAsync(convId, uid);
                }
            });

            return NextQuestionResponse.builder()
                    .conversationId(conversationId)
                    .turnNumber(currentTurn.getTurnNumber())
                    .aiQuestion(null)
                    .turnType(TurnType.QUESTION.name())
                    .lastTurn(true)
                    .conversationComplete(true)
                    .build();
        }

        // Determine next follow-up question
        String nextFollowUp = null;
        int nextFollowUpIndex = currentFollowUpIndex + 1;
        if (followUps != null && nextFollowUpIndex > 0 && nextFollowUpIndex <= followUps.size()) {
            nextFollowUp = followUps.get(nextFollowUpIndex - 1);
        }

        // AI decides: HINT (user struggling) or QUESTION (user answered well -> next follow-up)
        AdaptiveResult aiResult = generateAdaptiveResponse(
                topic, existingTurns, currentTurn, nextFollowUp, currentFollowUpIndex);

        int nextTurnNumber = currentTurn.getTurnNumber() + 1;

        SpeakingConversationTurn nextTurn = SpeakingConversationTurn.builder()
                .conversationId(conversationId)
                .turnNumber(nextTurnNumber)
                .aiQuestion(aiResult.response)
                .turnType(aiResult.turnType)
                .followUpIndex(TurnType.HINT.name().equals(aiResult.turnType) ? currentFollowUpIndex : nextFollowUpIndex)
                .build();
        turnRepository.save(nextTurn);

        conversation.setTotalTurns(nextTurnNumber);
        conversationRepository.save(conversation);

        // isLastTurn only if this was a QUESTION and it covers the final follow-up
        boolean isLastTurn = false;
        if (TurnType.QUESTION.name().equals(aiResult.turnType)) {
            isLastTurn = (nextFollowUpIndex + 1) >= totalExpectedQuestions;
        }

        return NextQuestionResponse.builder()
                .conversationId(conversationId)
                .turnNumber(nextTurnNumber)
                .aiQuestion(aiResult.response)
                .turnType(aiResult.turnType)
                .lastTurn(isLastTurn)
                .conversationComplete(false)
                .build();
    }

    @Override
    public ConversationResponse getConversation(UUID conversationId, UUID userId) {
        SpeakingConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found"));

        if (!Objects.equals(userId, conversation.getUserId()) && !authUtil.isAdmin()) {
            throw new ForbiddenException("Unauthorized: You can only access your own conversation");
        }

        SpeakingTopic topic = topicRepository.findById(conversation.getTopicId()).orElse(null);
        List<SpeakingConversationTurn> turns =
                turnRepository.findByConversationIdOrderByTurnNumberAsc(conversationId);

        return speakingMapper.toConversationResponse(conversation, topic, turns);
    }

    @Override
    public PageResponse<ConversationResponse> getConversationHistory(int page, int size, UUID userId) {
        UUID authUserId = authUtil.getUserId();

        if (!authUserId.equals(userId) && !authUtil.isAdmin()) {
            throw new ForbiddenException("Unauthorized: You can only access your own conversation history");
        }

        Page<SpeakingConversation> convPage = conversationRepository
                .findByUserIdOrderByStartedAtDesc(userId, PageRequest.of(page, size));

        List<ConversationResponse> items = convPage.getContent().stream()
                .map(conv -> {
                    SpeakingTopic topic = topicRepository.findById(conv.getTopicId()).orElse(null);
                    return speakingMapper.toConversationResponse(conv, topic, null);
                })
                .collect(Collectors.toList());

        return PageResponse.<ConversationResponse>builder()
                .items(items)
                .page(convPage.getNumber())
                .size(convPage.getSize())
                .totalElements(convPage.getTotalElements())
                .totalPages(convPage.getTotalPages())
                .build();
    }

    // --- Private helpers ---

    private AdaptiveResult generateAdaptiveResponse(SpeakingTopic topic,
                                                    List<SpeakingConversationTurn> existingTurns,
                                                    SpeakingConversationTurn currentTurn,
                                                    String nextFollowUp,
                                                    int currentFollowUpIndex) {


        try {
            String prompt = PromptBuilder.buildAdaptivePrompt(topic, existingTurns, currentTurn, nextFollowUp);
            AiAskResponse aiResponse = openClawService.systemCallAi(prompt);
            String raw = aiResponse.getAnswer().trim();

            // Parse JSON response
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(raw);
                String action = json.has("action") ? json.get("action").asText().toUpperCase() : TurnType.FOLLOWUP.name();
                String response = json.has("response") ? json.get("response").asText() : raw;

                if (!TurnType.HINT.name().equals(action) && !TurnType.FOLLOWUP.name().equals(action)) {
                    action = TurnType.FOLLOWUP.name();
                }
                String turnType = TurnType.HINT.name().equals(action) ? TurnType.HINT.name() : TurnType.QUESTION.name();
                return new AdaptiveResult(turnType, response);
            } catch (Exception parseEx) {
                log.warn("Failed to parse AI JSON response, treating as FOLLOWUP: {}", parseEx.getMessage());
                String cleaned = raw;
                if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                    cleaned = cleaned.substring(1, cleaned.length() - 1);
                }
                return new AdaptiveResult(TurnType.QUESTION.name(),
                        cleaned.isEmpty() ? (nextFollowUp != null ? nextFollowUp : "Could you tell me more?") : cleaned);
            }
        } catch (Exception e) {
            log.warn("AI adaptive response failed, using fallback: {}", e.getMessage());
            return new AdaptiveResult(TurnType.QUESTION.name(),
                    nextFollowUp != null ? nextFollowUp : "Could you elaborate on that?");
        }
    }

    private record AdaptiveResult(String turnType, String response) {
    }
}
