package com.swpts.enpracticebe.dto.response.speaking;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ConversationResponse {
    private UUID id;
    private UUID topicId;
    private String topicQuestion;
    private String topicPart;
    private String status;
    private Integer totalTurns;
    private Integer timeSpentSeconds;
    private Float fluencyScore;
    private Float lexicalScore;
    private Float grammarScore;
    private Float pronunciationScore;
    private Float overallBandScore;
    private String aiFeedback;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant startedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant completedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant gradedAt;
    private List<ConversationTurnResponse> turns;
}
