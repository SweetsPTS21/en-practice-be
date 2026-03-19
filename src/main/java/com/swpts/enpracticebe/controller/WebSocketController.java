package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.ai.ChatMessageRequest;
import com.swpts.enpracticebe.dto.request.speaking.SpeakingConversationMessage;
import com.swpts.enpracticebe.dto.request.speaking.SubmitTurnRequest;
import com.swpts.enpracticebe.dto.response.ai.AiChatStreamResponse;
import com.swpts.enpracticebe.dto.response.speaking.NextQuestionResponse;
import com.swpts.enpracticebe.dto.response.speaking.SpeakingConversationWsResponse;
import com.swpts.enpracticebe.service.ConversationSpeakingService;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.service.TextToSpeechService;
import com.swpts.enpracticebe.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private static final String REALTIME_CHAT_ERROR_MESSAGE = "Xin lỗi nha, hiện tại tôi không thể trả lời tin nhắn của bạn";

    private final OpenClawService openClawService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthUtil authUtil;
    private final ConversationSpeakingService conversationService;
    private final TextToSpeechService textToSpeechService;

    @MessageMapping("/realtime-chat")
    public void handleChatMessage(@Payload ChatMessageRequest message, Principal principal) {
        log.info("Received chat message: {}", message.getContent());

        UUID userId = authUtil.getUserId(principal);
        if (userId == null) {
            log.error("Could not get user ID from WebSocket session");
            return;
        }

        String topic = "/topic/realtime-chat/" + userId;

        try {
            openClawService.streamAi(message.getContent(), userId,
                    event -> messagingTemplate.convertAndSend(topic, event));
            log.info("Started realtime stream to user: {}", userId);
        } catch (Exception e) {
            log.error("Error starting realtime chat stream: {}", e.getMessage(), e);
            messagingTemplate.convertAndSend(topic,
                    AiChatStreamResponse.error(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                            REALTIME_CHAT_ERROR_MESSAGE));
        }
    }

    @MessageMapping("/speaking-conversation")
    public void handleSpeakingConversation(@Payload SpeakingConversationMessage message, Principal principal) {
        UUID userId = authUtil.getUserId(principal);
        if (userId == null) {
            log.error("Could not get user ID from WebSocket session for speaking conversation");
            return;
        }

        String topic = "/topic/speaking-conversation/" + userId;

        try {
            NextQuestionResponse result;

            if ("start".equalsIgnoreCase(message.getAction())) {
                log.info("Starting speaking conversation for topic: {}", message.getTopicId());
                result = conversationService.startConversation(message.getTopicId(), userId);
            } else {
                log.info("Submitting turn for conversation: {}", message.getConversationId());
                SubmitTurnRequest turnReq = new SubmitTurnRequest();
                turnReq.setTranscript(message.getTranscript());
                turnReq.setAudioUrl(message.getAudioUrl());
                turnReq.setTimeSpentSeconds(message.getTimeSpentSeconds());
                turnReq.setSpeechAnalytics(message.getSpeechAnalytics());
                result = conversationService.submitTurn(message.getConversationId(), turnReq, userId);
            }

            SpeakingConversationWsResponse wsResponse = SpeakingConversationWsResponse.builder()
                    .type(result.isConversationComplete() ? "CONVERSATION_COMPLETE" : "NEXT_QUESTION")
                    .conversationId(result.getConversationId())
                    .turnNumber(result.getTurnNumber())
                    .aiQuestion(result.getAiQuestion())
                    .turnType(result.getTurnType())
                    .lastTurn(result.isLastTurn())
                    .timestamp(Instant.now())
                    .build();

            if (result.getAiQuestion() != null && !result.getAiQuestion().isBlank()) {
                try {
                    byte[] audioBytes = textToSpeechService.synthesize(result.getAiQuestion(), null);
                    wsResponse.setAudioBase64(Base64.getEncoder().encodeToString(audioBytes));
                } catch (Exception e) {
                    log.warn("TTS synthesis failed, client will use fallback: {}", e.getMessage());
                }
            }

            messagingTemplate.convertAndSend(topic, wsResponse);
            log.info("Sent speaking conversation response to user: {}", userId);

        } catch (Exception e) {
            log.error("Error in speaking conversation: {}", e.getMessage(), e);
            messagingTemplate.convertAndSend(topic, SpeakingConversationWsResponse.error(e.getMessage()));
        }
    }
}
