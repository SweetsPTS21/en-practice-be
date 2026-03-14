package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.request.ai.OpenClawRequest;
import com.swpts.enpracticebe.dto.response.ai.AiAskResponse;
import com.swpts.enpracticebe.dto.response.ai.AiExplainResponse;
import com.swpts.enpracticebe.dto.response.ai.OpenClawResponse;
import com.swpts.enpracticebe.dto.response.dictionary.ExampleSentence;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.util.AuthUtil;
import com.swpts.enpracticebe.util.JsonUtil;
import com.swpts.enpracticebe.util.PromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class OpenClawServiceImpl implements OpenClawService {
    private final WebClient webClient;
    private final AuthUtil authUtil;
    private final ObjectMapper objectMapper;

    public OpenClawServiceImpl(@Value("${openclaw.gateway.url:http://127.0.0.1:18789}") String gatewayUrl,
                               @Value("${openclaw.gateway.token:abc}") String gatewayToken,
                               WebClient.Builder builder,
                               AuthUtil authUtil,
                               ObjectMapper objectMapper) {
        this.authUtil = authUtil;
        this.objectMapper = objectMapper;
        this.webClient = builder
                .baseUrl(gatewayUrl)
                .defaultHeader("Authorization", "Bearer " + gatewayToken)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-openclaw-agent-id", "main")
                .build();
    }

    private OpenClawRequest getOpenClawRequest(String prompt) {
        return getOpenClawRequest(prompt, authUtil.getUserId());
    }

    private OpenClawRequest getOpenClawRequest(String prompt, UUID userId) {
        OpenClawRequest request = new OpenClawRequest();
        request.setModel("openclaw");
        request.setUser("en-practice-user-" + userId);

        OpenClawRequest.Message msg = new OpenClawRequest.Message();
        msg.setRole("user");
        msg.setContent(prompt);

        request.setMessages(List.of(msg));
        return request;
    }

    private String getOpenClawResponse(OpenClawRequest request) {
        OpenClawResponse response = webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenClawResponse.class)
                .block();

        if (Objects.isNull(response)) {
            throw new RuntimeException("Failed to get response from OpenClaw");
        }

        return response.getChoices().get(0).getMessage().getContent();
    }

    @Override
    @Cacheable(value = "explainWord", key = "#word.trim().toLowerCase()")
    public AiExplainResponse explainWord(String word) {
        String prompt = PromptBuilder.buildExplainWordPrompt(word);

        try {

            OpenClawRequest request = getOpenClawRequest(prompt);
            String response = getOpenClawResponse(request);
            String jsonStr = JsonUtil.extractJson(response);
            JsonNode node = objectMapper.readTree(jsonStr);

            var examples = JsonUtil.parseJsonList(node.get("examples"), ExampleSentence.class);

            return AiExplainResponse.builder()
                    .word(word)
                    .ipa(node.get("ipa").asText())
                    .meaning(node.get("meaning").asText())
                    .wordType(node.get("wordType").asText())
                    .explanation(node.get("explanation").asText())
                    .examples(examples)
                    .sourceType(node.get("sourceType").asText())
                    .build();

        } catch (Exception e) {
            log.info("Failed to explain word {} :: {}", word, e.getMessage());
        }

        return AiExplainResponse.builder()
                .word(word)
                .explanation("Xin lỗi, tôi không thể giúp bạn lúc này ><")
                .build();
    }

    @Override
    public AiAskResponse askAi(String prompt) {
        OpenClawRequest request = getOpenClawRequest(prompt);
        String response = getOpenClawResponse(request);

        return AiAskResponse.builder()
                .answer(response)
                .build();
    }

    @Override
    public AiAskResponse askAi(String prompt, UUID userId) {
        OpenClawRequest request = getOpenClawRequest(prompt, userId);
        String response = getOpenClawResponse(request);

        return AiAskResponse.builder()
                .answer(response)
                .build();
    }
}
