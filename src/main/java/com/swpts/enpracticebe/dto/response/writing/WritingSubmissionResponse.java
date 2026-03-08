package com.swpts.enpracticebe.dto.response.writing;

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
public class WritingSubmissionResponse {
    private UUID id;
    private UUID taskId;
    private String taskTitle;
    private String taskType;
    private String essayContent;
    private Integer wordCount;
    private Integer timeSpentSeconds;
    private String status;

    // Grading scores (null when not yet graded)
    private Float taskResponseScore;
    private Float coherenceScore;
    private Float lexicalResourceScore;
    private Float grammarScore;
    private Float overallBandScore;
    private String aiFeedback;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant submittedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant gradedAt;
}
