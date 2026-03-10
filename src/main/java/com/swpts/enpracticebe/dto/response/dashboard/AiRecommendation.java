package com.swpts.enpracticebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendation {
    private String title;
    private String description;
    private String type; // LISTENING, READING, SPEAKING, WRITING, VOCAB
    private String difficulty; // Easy, Medium, Hard
    private String estimatedTime;
    private String reason;
    private int priority;
}
