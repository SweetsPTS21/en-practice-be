package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.speaking.SubmitTurnRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.speaking.ConversationResponse;
import com.swpts.enpracticebe.dto.response.speaking.NextQuestionResponse;

import java.util.UUID;

public interface ConversationSpeakingService {

    NextQuestionResponse startConversation(UUID topicId, UUID userId);

    NextQuestionResponse submitTurn(UUID conversationId, SubmitTurnRequest request, UUID userId);

    ConversationResponse getConversation(UUID conversationId, UUID userId);

    PageResponse<ConversationResponse> getConversationHistory(int page, int size, UUID userId);
}
