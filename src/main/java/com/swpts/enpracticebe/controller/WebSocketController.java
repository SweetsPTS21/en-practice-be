package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.ChatMessageRequest;
import com.swpts.enpracticebe.dto.request.SpeakingConversationMessage;
import com.swpts.enpracticebe.dto.request.SubmitTurnRequest;
import com.swpts.enpracticebe.dto.response.AiAskResponse;
import com.swpts.enpracticebe.dto.response.AiChatResponse;
import com.swpts.enpracticebe.dto.response.NextQuestionResponse;
import com.swpts.enpracticebe.dto.response.SpeakingConversationWsResponse;
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

        try {
            AiAskResponse result = openClawService.askAi(message.getContent(), userId);
            AiChatResponse response = AiChatResponse.buildResponse(result.getAnswer());
            messagingTemplate.convertAndSend("/topic/realtime-chat/" + userId, response);
            log.info("Sent response to user: {}", userId);
        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);
            AiChatResponse errorResponse = AiChatResponse.buildResponse(
                    "Xin lỗi nha, hiện tại tôi không thể trả lời tin nhắn của bạn");
            messagingTemplate.convertAndSend("/topic/realtime-chat/" + userId, errorResponse);
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
                result = conversationService.submitTurn(message.getConversationId(), turnReq, userId);
            }

            // Build WS response
            SpeakingConversationWsResponse wsResponse = SpeakingConversationWsResponse.builder()
                    .type(result.isConversationComplete() ? "CONVERSATION_COMPLETE" : "NEXT_QUESTION")
                    .conversationId(result.getConversationId())
                    .turnNumber(result.getTurnNumber())
                    .aiQuestion(result.getAiQuestion())
                    .lastTurn(result.isLastTurn())
                    .timestamp(Instant.now())
                    .build();

            // Synthesize TTS audio for AI question
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
