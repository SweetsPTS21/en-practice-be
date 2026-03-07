package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.SubmitTurnRequest;
import com.swpts.enpracticebe.dto.response.ConversationResponse;
import com.swpts.enpracticebe.dto.response.NextQuestionResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;

import java.util.UUID;

public interface ConversationSpeakingService {

    NextQuestionResponse startConversation(UUID topicId, UUID userId);

    NextQuestionResponse submitTurn(UUID conversationId, SubmitTurnRequest request, UUID userId);

    ConversationResponse getConversation(UUID conversationId, UUID userId);

    PageResponse<ConversationResponse> getConversationHistory(int page, int size, UUID userId);
}
