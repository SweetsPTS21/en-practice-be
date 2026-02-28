package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.ChatMessageRequest;
import com.swpts.enpracticebe.dto.response.AiAskResponse;
import com.swpts.enpracticebe.dto.response.AiChatResponse;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final OpenClawService openClawService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthUtil authUtil;

    @MessageMapping("/realtime-chat")
    public void handleChatMessage(@Payload ChatMessageRequest message, Principal principal) {
        log.info("Received chat message: {}", message.getContent());

        // Extract userId from STOMP session principal (set during CONNECT)
        UUID userId = authUtil.getUserId(principal);
        if (userId == null) {
            log.error("Could not get user ID from WebSocket session");
            return;
        }

        try {
            // Get AI response using only the content field
            AiAskResponse result = openClawService.askAi(message.getContent());
            AiChatResponse response = AiChatResponse.buildResponse(result.getAnswer());

            // Send response back to the specific user's topic
            messagingTemplate.convertAndSend("/topic/realtime-chat/" + userId, response);
            log.info("Sent response to user: {}", userId);

        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);

            AiChatResponse errorResponse = AiChatResponse.buildResponse(
                    "Xin lỗi, tôi không thể trả lời tin nhắn của bạn :((");

            messagingTemplate.convertAndSend("/topic/realtime-chat/" + userId, errorResponse);
        }
    }
}
