package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.OpenClawRequest;
import com.swpts.enpracticebe.dto.response.AiExplainResponse;
import com.swpts.enpracticebe.dto.response.OpenClawResponse;
import com.swpts.enpracticebe.util.AuthUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AiExplainService {
    private final WebClient webClient;
    private final AuthUtil authUtil;

    public AiExplainService(@Value("${openclaw.gateway.url:http://127.0.0.1:18789}") String gatewayUrl,
                            @Value("${openclaw.gateway.token:abc}") String gatewayToken,
                            WebClient.Builder builder,
                            AuthUtil authUtil) {
        this.authUtil = authUtil;
        this.webClient = builder
                .baseUrl(gatewayUrl)
                .defaultHeader("Authorization", "Bearer " + gatewayToken)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-openclaw-agent-id", "main")
                .build();
    }

    private OpenClawRequest getOpenClawRequest(String word) {
        String prompt = String.format(
                "Giải thích từ tiếng Anh: \"%s\". " +
                        "Bao gồm: 1) Phiên âm IPA, 2) Từ loại, " +
                        "3) Nghĩa tiếng Việt, 4) Ví dụ câu (EN + dịch VN). " +
                        "Trả lời ngắn gọn, rõ ràng, có chút hài hước.",
                word
        );

        UUID userId = authUtil.getUserId();
        OpenClawRequest request = new OpenClawRequest();
        request.setModel("openclaw");
        request.setUser("en-practice-user-" + userId);

        OpenClawRequest.Message msg = new OpenClawRequest.Message();
        msg.setRole("user");
        msg.setContent(prompt);

        request.setMessages(List.of(msg));
        return request;
    }

    public AiExplainResponse askAI(String word) {
        OpenClawRequest request = getOpenClawRequest(word);

        OpenClawResponse response = webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenClawResponse.class)
                .block();

        if (Objects.isNull(response)) {
            throw new RuntimeException("Failed to get response from OpenClaw");
        }

        var content = response.getChoices().get(0).getMessage().getContent();

        return AiExplainResponse.builder()
                .word(word)
                .explanation(content)
                .build();
    }
}
