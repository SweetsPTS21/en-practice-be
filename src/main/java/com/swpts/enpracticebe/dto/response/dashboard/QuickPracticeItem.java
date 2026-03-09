package com.swpts.enpracticebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickPracticeItem {
    private String id;
    private String title;
    private String type; // VOCAB, LISTENING, SPEAKING
    private String estimatedTime; // e.g. "3 mins"
    private String icon;
    private String path; // route path
}
