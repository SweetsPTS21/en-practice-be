package com.swpts.enpracticebe.dto.response;

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
public class SpeakingTopicListResponse {
    private UUID id;
    private String part;
    private String question;
    private String difficulty;
    private Instant createdAt;
}
