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
public class WritingTaskListResponse {
    private UUID id;
    private String taskType;
    private String title;
    private String difficulty;
    private Integer timeLimitMinutes;
    private Integer minWords;
    private Integer maxWords;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;
}
