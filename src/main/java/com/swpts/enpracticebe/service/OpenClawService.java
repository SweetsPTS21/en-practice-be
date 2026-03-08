package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.response.ai.AiAskResponse;
import com.swpts.enpracticebe.dto.response.ai.AiExplainResponse;

import java.util.UUID;

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
}
