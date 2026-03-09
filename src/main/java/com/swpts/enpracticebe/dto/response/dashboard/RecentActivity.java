package com.swpts.enpracticebe.dto.response.dashboard;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivity {
    private String id;
    private String title;
    private String type; // VOCAB, LISTENING, READING, SPEAKING, WRITING
    private String score; // Flexible score representation (e.g. "8/10", "6.5", "20 words")
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant timestamp;
}
