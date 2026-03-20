package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.response.ai.AiAskResponse;
import com.swpts.enpracticebe.dto.response.ai.AiChatStreamResponse;
import com.swpts.enpracticebe.dto.response.ai.AiExplainResponse;

import java.util.UUID;
import java.util.function.Consumer;

public interface OpenClawService {

    /**
     * Use AI to explain word
     *
     * @param word english word
     * @return ai explain response
     */
    AiExplainResponse explainWord(String word);

    /**
     * Ask AI using prompt
     *
     * @param prompt prompt
     * @return ai ask response
     */
    AiAskResponse askAi(String prompt);

    /**
     * Ask AI with user id (use for websocket)
     *
     * @param prompt prompt
     * @param userId user id
     * @return ai ask response
     */
    AiAskResponse askAi(String prompt, UUID userId);

    /**
     * Ask AI using a dedicated freestyle conversation session.
     *
     * @param prompt prompt
     * @param userId user id
     * @param conversationId conversation id
     * @return ai ask response
     */
    AiAskResponse askFreestyleConversationAi(String prompt, UUID userId, UUID conversationId);

    /**
     * Stream AI response for realtime websocket chat.
     *
     * @param prompt prompt
     * @param userId user id
     * @param eventConsumer callback for streaming events
     */
    void streamAi(String prompt, UUID userId, Consumer<AiChatStreamResponse> eventConsumer);

    /**
     * Get AI response for system operations
     *
     * @param prompt prompt
     * @return ai ask response
     */
    AiAskResponse systemCallAi(String prompt);
}
