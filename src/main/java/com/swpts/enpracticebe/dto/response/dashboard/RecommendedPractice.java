package com.swpts.enpracticebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedPractice {
    private String id;
    private String title;
    private String description;
    private String type; // e.g., VOCAB, LISTENING, READING, SPEAKING
    private String difficulty; // e.g., Medium, Hard
    private String estimatedTime; // e.g., "8 mins"
    private String path;
}
