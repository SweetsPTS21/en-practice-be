package com.swpts.enpracticebe.dto.response.speaking;

import com.swpts.enpracticebe.constant.VoiceName;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CustomConversationStepResponse {
    private UUID conversationId;
    private String title;
    private Integer turnNumber;
    private String aiMessage;
    private boolean conversationComplete;
    private boolean gradingEnabled;
    private String status;
    private Integer userTurnCount;
    private Integer maxUserTurns;
    private VoiceName voiceName;
}
