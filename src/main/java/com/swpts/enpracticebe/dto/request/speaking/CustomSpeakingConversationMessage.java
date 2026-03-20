package com.swpts.enpracticebe.dto.request.speaking;

import com.swpts.enpracticebe.constant.CustomConversationExpertise;
import com.swpts.enpracticebe.constant.CustomConversationPersonality;
import com.swpts.enpracticebe.constant.CustomConversationStyle;
import com.swpts.enpracticebe.dto.speech.SpeechAnalyticsDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomSpeakingConversationMessage {
    private String action; // start, submit, finish
    private UUID conversationId;
    private String topic;
    private CustomConversationStyle style;
    private CustomConversationPersonality personality;
    private CustomConversationExpertise expertise;
    private Boolean gradingEnabled;
    private String transcript;
    private String audioUrl;
    private Integer timeSpentSeconds;
    private SpeechAnalyticsDto speechAnalytics;
}
