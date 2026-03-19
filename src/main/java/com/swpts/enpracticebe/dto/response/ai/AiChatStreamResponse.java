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
public class AiChatStreamResponse {
    private String requestId;
    private String messageId;
    private String type;
    private String content;
    private String senderId;
    private boolean isFinal;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant timestamp;

    public static AiChatStreamResponse start(String requestId, String messageId) {
        return base(requestId, messageId)
                .type("START")
                .content("")
                .isFinal(false)
                .build();
    }

    public static AiChatStreamResponse delta(String requestId, String messageId, String content) {
        return base(requestId, messageId)
                .type("DELTA")
                .content(content)
                .isFinal(false)
                .build();
    }

    public static AiChatStreamResponse complete(String requestId, String messageId, String content) {
        return base(requestId, messageId)
                .type("COMPLETE")
                .content(content)
                .isFinal(true)
                .build();
    }

    public static AiChatStreamResponse error(String requestId, String messageId, String content) {
        return base(requestId, messageId)
                .type("ERROR")
                .content(content)
                .isFinal(true)
                .build();
    }

    private static AiChatStreamResponseBuilder base(String requestId, String messageId) {
        return AiChatStreamResponse.builder()
                .requestId(requestId)
                .messageId(messageId)
                .senderId(Constants.AI_NAME)
                .timestamp(Instant.now());
    }
}
