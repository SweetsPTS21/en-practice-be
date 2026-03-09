package com.swpts.enpracticebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartReminder {
    private String title;
    private String message;
    private String type; // e.g., WARNING, INFO
    private String ctaText;
    private String ctaPath;
}
