package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.response.AiAskResponse;
import com.swpts.enpracticebe.dto.response.AiExplainResponse;

import java.util.UUID;

public interface OpenClawService {
    AiExplainResponse explainWord(String word);

    AiAskResponse askAi(String prompt);

    AiAskResponse askAi(String prompt, UUID userId);
}
