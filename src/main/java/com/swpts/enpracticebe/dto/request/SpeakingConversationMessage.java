package com.swpts.enpracticebe.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingConversationMessage {
    private String action; // "start" or "submit"
    private UUID topicId;
    private UUID conversationId;
    private String transcript;
    private String audioUrl;
    private Integer timeSpentSeconds;
}
