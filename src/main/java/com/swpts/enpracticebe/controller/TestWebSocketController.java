package com.swpts.enpracticebe.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class TestWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    public TestWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/test")
    public void handleTestMessage(@Payload String message) {
        log.info("Received test message: {}", message);
        
        // Echo the message back to all subscribers
        messagingTemplate.convertAndSend("/topic/test", "Echo: " + message);
    }
}
