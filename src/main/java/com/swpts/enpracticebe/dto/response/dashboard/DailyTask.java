package com.swpts.enpracticebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTask {
    private String id;
    private String title;
    private String description;
    private boolean completed;
    private String type; // VOCAB, LISTENING, READING, SPEAKING, WRITING
}
