package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.speaking.StartCustomConversationRequest;
import com.swpts.enpracticebe.dto.request.speaking.SubmitCustomConversationTurnRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.speaking.CustomConversationResponse;
import com.swpts.enpracticebe.dto.response.speaking.CustomConversationStepResponse;

import java.util.UUID;

public interface CustomConversationSpeakingService {

    CustomConversationStepResponse startConversation(StartCustomConversationRequest request, UUID userId);

    CustomConversationStepResponse submitTurn(UUID conversationId, SubmitCustomConversationTurnRequest request, UUID userId);

    CustomConversationStepResponse finishConversation(UUID conversationId, UUID userId);

    CustomConversationResponse getConversation(UUID conversationId, UUID userId);

    PageResponse<CustomConversationResponse> getConversationHistory(int page, int size, UUID userId);
}
