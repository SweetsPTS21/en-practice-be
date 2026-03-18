package com.swpts.enpracticebe.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.response.admin.AdminSpeakingTopicResponse;
import com.swpts.enpracticebe.dto.response.speaking.*;
import com.swpts.enpracticebe.dto.speech.SpeechAnalyticsDto;
import com.swpts.enpracticebe.entity.SpeakingAttempt;
import com.swpts.enpracticebe.entity.SpeakingConversation;
import com.swpts.enpracticebe.entity.SpeakingConversationTurn;
import com.swpts.enpracticebe.entity.SpeakingTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpeakingMapper {

    private final ObjectMapper objectMapper;

    // ─── Helper ───────────────────────────────────────────────────────────────

    /** Safely deserialize speech_data_json → SpeechAnalyticsDto; returns null on failure */
    private SpeechAnalyticsDto parseSpeechData(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, SpeechAnalyticsDto.class);
        } catch (Exception e) {
            log.warn("Failed to parse speech_data_json: {}", e.getMessage());
            return null;
        }
    }

    // ─── Topic mappings ───────────────────────────────────────────────────────

    public SpeakingTopicListResponse toListResponse(SpeakingTopic topic) {
        return SpeakingTopicListResponse.builder()
                .id(topic.getId())
                .part(topic.getPart().name())
                .question(topic.getQuestion())
                .difficulty(topic.getDifficulty().name())
                .createdAt(topic.getCreatedAt())
                .build();
    }

    public SpeakingTopicResponse toTopicResponse(SpeakingTopic topic) {
        return SpeakingTopicResponse.builder()
                .id(topic.getId())
                .part(topic.getPart().name())
                .question(topic.getQuestion())
                .cueCard(topic.getCueCard())
                .followUpQuestions(topic.getFollowUpQuestions())
                .difficulty(topic.getDifficulty().name())
                .isPublished(topic.getIsPublished())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .build();
    }

    // ─── Attempt mappings ─────────────────────────────────────────────────────

    public SpeakingAttemptResponse toAttemptResponse(SpeakingAttempt attempt, SpeakingTopic topic) {
        return SpeakingAttemptResponse.builder()
                .id(attempt.getId())
                .topicId(attempt.getTopicId())
                .topicQuestion(topic != null ? topic.getQuestion() : "Unknown")
                .topicPart(topic != null ? topic.getPart().name() : null)
                .audioUrl(attempt.getAudioUrl())
                .transcript(attempt.getTranscript())
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
                .status(attempt.getStatus().name())
                .fluencyScore(attempt.getFluencyScore())
                .lexicalScore(attempt.getLexicalScore())
                .grammarScore(attempt.getGrammarScore())
                .pronunciationScore(attempt.getPronunciationScore())
                .overallBandScore(attempt.getOverallBandScore())
                .aiFeedback(attempt.getAiFeedback())
                .speechAnalytics(parseSpeechData(attempt.getSpeechDataJson()))
                .submittedAt(attempt.getSubmittedAt())
                .gradedAt(attempt.getGradedAt())
                .build();
    }

    // ─── Admin topic ──────────────────────────────────────────────────────────

    public AdminSpeakingTopicResponse toAdminResponse(SpeakingTopic topic) {
        return AdminSpeakingTopicResponse.builder()
                .id(topic.getId())
                .part(topic.getPart().name())
                .question(topic.getQuestion())
                .cueCard(topic.getCueCard())
                .followUpQuestions(topic.getFollowUpQuestions())
                .aiGradingPrompt(topic.getAiGradingPrompt())
                .difficulty(topic.getDifficulty().name())
                .isPublished(topic.getIsPublished())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .build();
    }

    // ─── Conversation mappings ────────────────────────────────────────────────

    public ConversationResponse toConversationResponse(SpeakingConversation conv, SpeakingTopic topic,
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
                            .speechAnalytics(parseSpeechData(t.getSpeechDataJson()))
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
