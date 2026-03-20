package com.swpts.enpracticebe.dto.response.speaking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swpts.enpracticebe.constant.CustomConversationExpertise;
import com.swpts.enpracticebe.constant.CustomConversationPersonality;
import com.swpts.enpracticebe.constant.CustomConversationStyle;
import com.swpts.enpracticebe.constant.VoiceName;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CustomConversationResponse {
    private UUID id;
    private String title;
    private String topic;
    private CustomConversationStyle style;
    private CustomConversationPersonality personality;
    private CustomConversationExpertise expertise;
    private VoiceName voiceName;
    private Boolean gradingEnabled;
    private String status;
    private Integer maxUserTurns;
    private Integer userTurnCount;
    private Integer totalTurns;
    private Integer timeSpentSeconds;
    private Float fluencyScore;
    private Float vocabularyScore;
    private Float coherenceScore;
    private Float pronunciationScore;
    private Float overallScore;
    private String aiFeedback;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant startedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant completedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant gradedAt;
    private List<CustomConversationTurnResponse> turns;
}
