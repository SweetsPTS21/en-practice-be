package com.swpts.enpracticebe.dto.response.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swpts.enpracticebe.constant.Constants;
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
public class AiChatResponse {
    private String id;
    private String content;
    private String senderId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant timestamp;

    public static AiChatResponse buildResponse(String content) {
        return AiChatResponse.builder()
                .id(UUID.randomUUID().toString())
                .content(content)
                .senderId(Constants.AI_NAME)
                .timestamp(Instant.now())
                .build();
    }
}
