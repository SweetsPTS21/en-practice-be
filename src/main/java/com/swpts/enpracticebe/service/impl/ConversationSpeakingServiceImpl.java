package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.request.SubmitTurnRequest;
import com.swpts.enpracticebe.dto.response.*;
import com.swpts.enpracticebe.dto.response.AiAskResponse;
import com.swpts.enpracticebe.entity.SpeakingConversation;
import com.swpts.enpracticebe.entity.SpeakingConversationTurn;
import com.swpts.enpracticebe.entity.SpeakingTopic;
import com.swpts.enpracticebe.repository.SpeakingConversationRepository;
import com.swpts.enpracticebe.repository.SpeakingConversationTurnRepository;
import com.swpts.enpracticebe.repository.SpeakingTopicRepository;
import com.swpts.enpracticebe.service.ConversationSpeakingService;
import com.swpts.enpracticebe.service.OpenClawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConversationSpeakingServiceImpl implements ConversationSpeakingService {

    private final SpeakingConversationRepository conversationRepository;
    private final SpeakingConversationTurnRepository turnRepository;
    private final SpeakingTopicRepository topicRepository;
    private final OpenClawService openClawService;
    private final ConversationGradingService gradingService;

    public ConversationSpeakingServiceImpl(
            SpeakingConversationRepository conversationRepository,
            SpeakingConversationTurnRepository turnRepository,
            SpeakingTopicRepository topicRepository,
            OpenClawService openClawService,
            ConversationGradingService gradingService) {
        this.conversationRepository = conversationRepository;
        this.turnRepository = turnRepository;
        this.topicRepository = topicRepository;
        this.openClawService = openClawService;
        this.gradingService = gradingService;
    }

    @Override
    @Transactional
    public NextQuestionResponse startConversation(UUID topicId, UUID userId) {

        SpeakingTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found: " + topicId));

        if (!topic.getIsPublished()) {
            throw new RuntimeException("Topic is not published");
        }

        // Create conversation
        SpeakingConversation conversation = SpeakingConversation.builder()
                .userId(userId)
                .topicId(topicId)
                .status(SpeakingConversation.ConversationStatus.IN_PROGRESS)
                .totalTurns(0)
                .build();
        conversation = conversationRepository.save(conversation);

        // Create first turn with topic's main question
        SpeakingConversationTurn firstTurn = SpeakingConversationTurn.builder()
                .conversationId(conversation.getId())
                .turnNumber(1)
                .aiQuestion(topic.getQuestion())
                .build();
        turnRepository.save(firstTurn);

        conversation.setTotalTurns(1);
        conversationRepository.save(conversation);

        // Determine if this is also the last turn (no follow-ups)
        int totalExpectedTurns = 1 + (topic.getFollowUpQuestions() != null ? topic.getFollowUpQuestions().size() : 0);

        return NextQuestionResponse.builder()
                .conversationId(conversation.getId())
                .turnNumber(1)
                .aiQuestion(topic.getQuestion())
                .lastTurn(totalExpectedTurns <= 1)
                .conversationComplete(false)
                .build();
    }

    @Override
    @Transactional
    public NextQuestionResponse submitTurn(UUID conversationId, SubmitTurnRequest request, UUID userId) {

        SpeakingConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));

        if (conversation.getStatus() != SpeakingConversation.ConversationStatus.IN_PROGRESS) {
            throw new RuntimeException("Conversation is not in progress");
        }

        SpeakingTopic topic = topicRepository.findById(conversation.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        // Get all existing turns
        List<SpeakingConversationTurn> existingTurns =
                turnRepository.findByConversationIdOrderByTurnNumberAsc(conversationId);

        // Find the current (latest unanswered) turn and save user's response
        SpeakingConversationTurn currentTurn = existingTurns.stream()
                .filter(t -> t.getUserTranscript() == null || t.getUserTranscript().isBlank())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No pending turn found"));

        currentTurn.setUserTranscript(request.getTranscript());
        currentTurn.setAudioUrl(request.getAudioUrl());
        currentTurn.setTimeSpentSeconds(request.getTimeSpentSeconds());
        turnRepository.save(currentTurn);

        // Determine total expected turns (1 main + follow-ups)
        List<String> followUps = topic.getFollowUpQuestions();
        int totalExpectedTurns = 1 + (followUps != null ? followUps.size() : 0);
        int currentTurnNumber = currentTurn.getTurnNumber();

        // If all turns answered -> complete conversation
        if (currentTurnNumber >= totalExpectedTurns) {
            conversation.setStatus(SpeakingConversation.ConversationStatus.COMPLETED);
            conversation.setCompletedAt(Instant.now());

            // Calculate total time
            int totalTime = existingTurns.stream()
                    .mapToInt(t -> t.getTimeSpentSeconds() != null ? t.getTimeSpentSeconds() : 0)
                    .sum();
            conversation.setTimeSpentSeconds(totalTime);
            conversationRepository.save(conversation);

            // Trigger async grading after commit
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
                    .turnNumber(currentTurnNumber)
                    .aiQuestion(null)
                    .lastTurn(true)
                    .conversationComplete(true)
                    .build();
        }

        // Generate next question using AI
        String nextQuestion = generateNextQuestion(topic, existingTurns, currentTurnNumber, followUps, userId);

        int nextTurnNumber = currentTurnNumber + 1;
        SpeakingConversationTurn nextTurn = SpeakingConversationTurn.builder()
                .conversationId(conversationId)
                .turnNumber(nextTurnNumber)
                .aiQuestion(nextQuestion)
                .build();
        turnRepository.save(nextTurn);

        conversation.setTotalTurns(nextTurnNumber);
        conversationRepository.save(conversation);

        boolean isLastTurn = nextTurnNumber >= totalExpectedTurns;

        return NextQuestionResponse.builder()
                .conversationId(conversationId)
                .turnNumber(nextTurnNumber)
                .aiQuestion(nextQuestion)
                .lastTurn(isLastTurn)
                .conversationComplete(false)
                .build();
    }

    @Override
    public ConversationResponse getConversation(UUID conversationId, UUID userId) {
        SpeakingConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        SpeakingTopic topic = topicRepository.findById(conversation.getTopicId()).orElse(null);
        List<SpeakingConversationTurn> turns =
                turnRepository.findByConversationIdOrderByTurnNumberAsc(conversationId);

        return toConversationResponse(conversation, topic, turns);
    }

    @Override
    public PageResponse<ConversationResponse> getConversationHistory(int page, int size, UUID userId) {
        Page<SpeakingConversation> convPage = conversationRepository
                .findByUserIdOrderByStartedAtDesc(userId, PageRequest.of(page, size));

        List<ConversationResponse> items = convPage.getContent().stream()
                .map(conv -> {
                    SpeakingTopic topic = topicRepository.findById(conv.getTopicId()).orElse(null);
                    return toConversationResponse(conv, topic, null);
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

    private String generateNextQuestion(SpeakingTopic topic, List<SpeakingConversationTurn> existingTurns,
                                        int currentTurnNumber, List<String> followUps, UUID userId) {
        // Build conversation history
        StringBuilder history = new StringBuilder();
        for (SpeakingConversationTurn turn : existingTurns) {
            history.append("Examiner: ").append(turn.getAiQuestion()).append("\n");
            if (turn.getUserTranscript() != null) {
                history.append("Student: ").append(turn.getUserTranscript()).append("\n");
            }
        }

        // The follow-up question to base on
        String suggestedFollowUp = followUps.get(currentTurnNumber - 1); // 0-indexed from follow-ups

        String prompt = String.format("""
                You are a friendly, professional IELTS Speaking examiner conducting a live interview.
                
                **Topic:** %s
                **Part:** %s
                
                **Conversation so far:**
                %s
                
                **Suggested next question:** %s
                
                Your task:
                1. First, respond naturally and briefly to what the student just said. Acknowledge their answer \
                with a short, genuine comment (1-2 sentences max). You can be slightly witty or add a touch of \
                light humor to keep the mood relaxed, but stay professional.
                2. Then, smoothly transition into the next question based on the suggested follow-up above. \
                Adapt the question naturally so it flows from your comment and the conversation context.
                
                Important rules:
                - Your response should sound like natural spoken English, as if you're having a real conversation.
                - Do NOT use any prefixes like "Examiner:" or "Question:".
                - Do NOT use markdown formatting or bullet points.
                - Keep the total response concise (3-4 sentences max: brief comment + question).
                - The question should cover the same topic area as the suggested follow-up.
                
                Respond with ONLY the examiner's spoken dialogue, nothing else.
                """,
                topic.getQuestion(),
                topic.getPart().name(),
                history.toString(),
                suggestedFollowUp);

        try {
            AiAskResponse aiResponse = openClawService.askAi(prompt, userId);
            String nextQ = aiResponse.getAnswer().trim();
            // Clean up if AI adds quotes or prefixes
            if (nextQ.startsWith("\"") && nextQ.endsWith("\"")) {
                nextQ = nextQ.substring(1, nextQ.length() - 1);
            }
            // Remove any "Examiner:" prefix the AI might add
            if (nextQ.toLowerCase().startsWith("examiner:")) {
                nextQ = nextQ.substring(9).trim();
            }
            return nextQ.isEmpty() ? suggestedFollowUp : nextQ;
        } catch (Exception e) {
            log.warn("AI next question generation failed, using suggested follow-up: {}", e.getMessage());
            return suggestedFollowUp;
        }
    }

    private ConversationResponse toConversationResponse(SpeakingConversation conv, SpeakingTopic topic,
                                                         List<SpeakingConversationTurn> turns) {
        List<ConversationTurnResponse> turnResponses = null;
        if (turns != null) {
            turnResponses = turns.stream()
                    .map(t -> ConversationTurnResponse.builder()
                            .id(t.getId())
                            .turnNumber(t.getTurnNumber())
                            .aiQuestion(t.getAiQuestion())
                            .userTranscript(t.getUserTranscript())
                            .audioUrl(t.getAudioUrl())
                            .timeSpentSeconds(t.getTimeSpentSeconds())
                            .createdAt(t.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
        }

        return ConversationResponse.builder()
                .id(conv.getId())
                .topicId(conv.getTopicId())
                .topicQuestion(topic != null ? topic.getQuestion() : null)
                .topicPart(topic != null ? topic.getPart().name() : null)
                .status(conv.getStatus().name())
                .totalTurns(conv.getTotalTurns())
                .timeSpentSeconds(conv.getTimeSpentSeconds())
                .fluencyScore(conv.getFluencyScore())
                .lexicalScore(conv.getLexicalScore())
                .grammarScore(conv.getGrammarScore())
                .pronunciationScore(conv.getPronunciationScore())
                .overallBandScore(conv.getOverallBandScore())
                .aiFeedback(conv.getAiFeedback())
                .startedAt(conv.getStartedAt())
                .completedAt(conv.getCompletedAt())
                .gradedAt(conv.getGradedAt())
                .turns(turnResponses)
                .build();
    }

}
