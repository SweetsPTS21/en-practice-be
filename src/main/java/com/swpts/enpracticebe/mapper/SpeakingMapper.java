package com.swpts.enpracticebe.mapper;

import com.swpts.enpracticebe.dto.response.admin.AdminSpeakingTopicResponse;
import com.swpts.enpracticebe.dto.response.speaking.*;
import com.swpts.enpracticebe.entity.SpeakingAttempt;
import com.swpts.enpracticebe.entity.SpeakingConversation;
import com.swpts.enpracticebe.entity.SpeakingConversationTurn;
import com.swpts.enpracticebe.entity.SpeakingTopic;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpeakingMapper {
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
                .submittedAt(attempt.getSubmittedAt())
                .gradedAt(attempt.getGradedAt())
                .build();
    }

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
