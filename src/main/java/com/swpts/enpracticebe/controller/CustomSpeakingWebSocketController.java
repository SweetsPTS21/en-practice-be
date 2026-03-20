package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.speaking.CustomSpeakingConversationMessage;
import com.swpts.enpracticebe.dto.request.speaking.StartCustomConversationRequest;
import com.swpts.enpracticebe.dto.request.speaking.SubmitCustomConversationTurnRequest;
import com.swpts.enpracticebe.dto.response.speaking.CustomConversationStepResponse;
import com.swpts.enpracticebe.dto.response.speaking.CustomSpeakingConversationWsResponse;
import com.swpts.enpracticebe.service.CustomConversationSpeakingService;
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
public class CustomSpeakingWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final AuthUtil authUtil;
    private final CustomConversationSpeakingService customConversationSpeakingService;
    private final TextToSpeechService textToSpeechService;

    @MessageMapping("/custom-speaking-conversation")
    public void handleCustomSpeakingConversation(@Payload CustomSpeakingConversationMessage message,
                                                 Principal principal) {
        UUID userId = authUtil.getUserId(principal);
        if (userId == null) {
            log.error("Could not get user ID from WebSocket session for custom speaking conversation");
            return;
        }

        String topic = "/topic/custom-speaking-conversation/" + userId;

        try {
            CustomConversationStepResponse result;
            if ("start".equalsIgnoreCase(message.getAction())) {
                StartCustomConversationRequest request = new StartCustomConversationRequest();
                request.setTopic(message.getTopic());
                request.setStyle(message.getStyle());
                request.setPersonality(message.getPersonality());
                request.setExpertise(message.getExpertise());
                request.setVoiceName(message.getVoiceName());
                request.setGradingEnabled(Boolean.TRUE.equals(message.getGradingEnabled()));
                result = customConversationSpeakingService.startConversation(request, userId);
            } else if ("finish".equalsIgnoreCase(message.getAction())) {
                result = customConversationSpeakingService.finishConversation(message.getConversationId(), userId);
            } else {
                SubmitCustomConversationTurnRequest request = new SubmitCustomConversationTurnRequest();
                request.setTranscript(message.getTranscript());
                request.setAudioUrl(message.getAudioUrl());
                request.setTimeSpentSeconds(message.getTimeSpentSeconds());
                request.setSpeechAnalytics(message.getSpeechAnalytics());
                result = customConversationSpeakingService.submitTurn(message.getConversationId(), request, userId);
            }

            CustomSpeakingConversationWsResponse wsResponse = CustomSpeakingConversationWsResponse.builder()
                    .type(result.isConversationComplete() ? "CONVERSATION_COMPLETE" : "AI_MESSAGE")
                    .conversationId(result.getConversationId())
                    .title(result.getTitle())
                    .turnNumber(result.getTurnNumber())
                    .aiMessage(result.getAiMessage())
                    .status(result.getStatus())
                    .userTurnCount(result.getUserTurnCount())
                    .maxUserTurns(result.getMaxUserTurns())
                    .timestamp(Instant.now())
                    .build();

            if (result.getAiMessage() != null && !result.getAiMessage().isBlank()) {
                try {
                    byte[] audioBytes = textToSpeechService.synthesize(result.getAiMessage(), result.getVoiceName());
                    wsResponse.setAudioBase64(Base64.getEncoder().encodeToString(audioBytes));
                } catch (Exception e) {
                    log.warn("TTS synthesis failed for custom conversation: {}", e.getMessage());
                }
            }

            messagingTemplate.convertAndSend(topic, wsResponse);
        } catch (Exception e) {
            log.error("Error in custom speaking conversation: {}", e.getMessage(), e);
            messagingTemplate.convertAndSend(topic, CustomSpeakingConversationWsResponse.error(e.getMessage()));
        }
    }
}
