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
                .turnType("QUESTION")
                .followUpIndex(0)
                .build();
        turnRepository.save(firstTurn);

        conversation.setTotalTurns(1);
        conversationRepository.save(conversation);

        int totalExpectedQuestions = 1 + (topic.getFollowUpQuestions() != null ? topic.getFollowUpQuestions().size() : 0);

        return NextQuestionResponse.builder()
                .conversationId(conversation.getId())
                .turnNumber(1)
                .aiQuestion(topic.getQuestion())
                .turnType("QUESTION")
                .lastTurn(totalExpectedQuestions <= 1)
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

        List<SpeakingConversationTurn> existingTurns =
                turnRepository.findByConversationIdOrderByTurnNumberAsc(conversationId);

        // Find the current unanswered turn and save user's response
        SpeakingConversationTurn currentTurn = existingTurns.stream()
                .filter(t -> t.getUserTranscript() == null || t.getUserTranscript().isBlank())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No pending turn found"));

        currentTurn.setUserTranscript(request.getTranscript());
        currentTurn.setAudioUrl(request.getAudioUrl());
        currentTurn.setTimeSpentSeconds(request.getTimeSpentSeconds());
        turnRepository.save(currentTurn);

        // Progress tracking: count only answered QUESTION turns
        List<String> followUps = topic.getFollowUpQuestions();
        int totalExpectedQuestions = 1 + (followUps != null ? followUps.size() : 0);

        long answeredQuestionCount = existingTurns.stream()
                .filter(t -> "QUESTION".equals(t.getTurnType())
                        && t.getUserTranscript() != null
                        && !t.getUserTranscript().isBlank())
                .count();

        // Current follow-up index from the last QUESTION turn
        int currentFollowUpIndex = existingTurns.stream()
                .filter(t -> "QUESTION".equals(t.getTurnType()))
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
                    .turnType("QUESTION")
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
                topic, existingTurns, currentTurn, nextFollowUp, currentFollowUpIndex, userId);

        int nextTurnNumber = currentTurn.getTurnNumber() + 1;

        SpeakingConversationTurn nextTurn = SpeakingConversationTurn.builder()
                .conversationId(conversationId)
                .turnNumber(nextTurnNumber)
                .aiQuestion(aiResult.response)
                .turnType(aiResult.turnType)
                .followUpIndex("HINT".equals(aiResult.turnType) ? currentFollowUpIndex : nextFollowUpIndex)
                .build();
        turnRepository.save(nextTurn);

        conversation.setTotalTurns(nextTurnNumber);
        conversationRepository.save(conversation);

        // isLastTurn only if this was a QUESTION and it covers the final follow-up
        boolean isLastTurn = false;
        if ("QUESTION".equals(aiResult.turnType)) {
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

    private record AdaptiveResult(String turnType, String response) {}

    private AdaptiveResult generateAdaptiveResponse(SpeakingTopic topic,
                                                     List<SpeakingConversationTurn> existingTurns,
                                                     SpeakingConversationTurn currentTurn,
                                                     String nextFollowUp,
                                                     int currentFollowUpIndex,
                                                     UUID userId) {
        StringBuilder history = new StringBuilder();
        for (SpeakingConversationTurn turn : existingTurns) {
            String label = "HINT".equals(turn.getTurnType()) ? "Examiner (hint)" : "Examiner";
            history.append(label).append(": ").append(turn.getAiQuestion()).append("\n");
            if (turn.getUserTranscript() != null) {
                history.append("Student: ").append(turn.getUserTranscript()).append("\n");
            }
        }

        String nextFollowUpSection = nextFollowUp != null
                ? "**Next follow-up question to move to (if student is ready):** " + nextFollowUp
                : "**No more follow-up questions remaining** — if the student answered well, this is the final turn.";

        String prompt = String.format("""
                You are a friendly, professional IELTS Speaking examiner conducting a live interview.
                
                **Topic:** %s
                **Part:** %s
                
                **Conversation so far:**
                %s
                
                **Student's latest answer:** %s
                
                %s
                
                Your task: Analyze the student's latest answer and decide ONE of these actions:
                
                **Action "HINT"** — Choose this if:
                - The student's answer is very short (only a few words), vague, or off-topic
                - The student seems to be struggling, hesitating, or says phrases like "I don't know", \
                "I'm not sure", "um...", or gives a very generic answer
                - The student explicitly asks for help or seems confused
                
                If you choose HINT:
                - Encourage the student warmly and naturally (e.g., "That's okay, no worries!", "Good start!")
                - Give them a helpful hint or suggestion to guide their thinking \
                (e.g., "Think about a time when...", "You could talk about...", "Maybe consider...")
                - End your response by naturally asking something like: \
                "Would you like me to give you another hint, or are you ready to give it a try?"
                - Keep it friendly, encouraging, and slightly humorous
                
                **Action "FOLLOWUP"** — Choose this if:
                - The student gave a reasonable, substantive answer (even if imperfect)
                - The student indicates they're ready to move on or ready to answer
                - The student responded to a hint by giving a proper answer
                
                If you choose FOLLOWUP:
                - Briefly comment on their answer naturally (1-2 sentences, can be witty/humorous)
                - Then smoothly transition to the next follow-up question
                - Adapt the question naturally so it flows from the conversation
                
                IMPORTANT: Respond with EXACTLY this JSON format, nothing else:
                {"action": "HINT" or "FOLLOWUP", "response": "your spoken response here"}
                
                Rules for the "response" field:
                - Natural spoken English, as if having a real conversation
                - No prefixes like "Examiner:" or "Question:"
                - No markdown formatting
                - Concise (3-5 sentences max)
                """,
                topic.getQuestion(),
                topic.getPart().name(),
                history.toString(),
                currentTurn.getUserTranscript(),
                nextFollowUpSection);

        try {
            AiAskResponse aiResponse = openClawService.askAi(prompt, userId);
            String raw = aiResponse.getAnswer().trim();

            // Parse JSON response
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(raw);
                String action = json.has("action") ? json.get("action").asText().toUpperCase() : "FOLLOWUP";
                String response = json.has("response") ? json.get("response").asText() : raw;

                if (!"HINT".equals(action) && !"FOLLOWUP".equals(action)) {
                    action = "FOLLOWUP";
                }
                String turnType = "HINT".equals(action) ? "HINT" : "QUESTION";
                return new AdaptiveResult(turnType, response);
            } catch (Exception parseEx) {
                log.warn("Failed to parse AI JSON response, treating as FOLLOWUP: {}", parseEx.getMessage());
                String cleaned = raw;
                if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                    cleaned = cleaned.substring(1, cleaned.length() - 1);
                }
                return new AdaptiveResult("QUESTION",
                        cleaned.isEmpty() ? (nextFollowUp != null ? nextFollowUp : "Could you tell me more?") : cleaned);
            }
        } catch (Exception e) {
            log.warn("AI adaptive response failed, using fallback: {}", e.getMessage());
            return new AdaptiveResult("QUESTION",
                    nextFollowUp != null ? nextFollowUp : "Could you elaborate on that?");
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
                            .turnType(t.getTurnType())
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
