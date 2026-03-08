package com.swpts.enpracticebe.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StartConversationRequest {
    @NotNull(message = "Topic ID is required")
    private UUID topicId;
}
