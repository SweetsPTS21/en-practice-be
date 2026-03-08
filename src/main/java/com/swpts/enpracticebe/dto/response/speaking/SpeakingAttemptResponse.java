package com.swpts.enpracticebe.dto.response.speaking;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingAttemptResponse {
    private UUID id;
    private UUID topicId;
    private String topicQuestion;
    private String topicPart;
    private String audioUrl;
    private String transcript;
    private Integer timeSpentSeconds;
    private String status;

    // Grading scores
    private Float fluencyScore;
    private Float lexicalScore;
    private Float grammarScore;
    private Float pronunciationScore;
    private Float overallBandScore;
    private String aiFeedback;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant submittedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant gradedAt;
}
