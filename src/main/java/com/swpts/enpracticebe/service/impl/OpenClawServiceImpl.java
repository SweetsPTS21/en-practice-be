package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.request.ai.OpenClawRequest;
import com.swpts.enpracticebe.dto.response.ai.AiAskResponse;
import com.swpts.enpracticebe.dto.response.ai.AiChatStreamResponse;
import com.swpts.enpracticebe.dto.response.ai.AiExplainResponse;
import com.swpts.enpracticebe.dto.response.ai.OpenClawResponse;
import com.swpts.enpracticebe.dto.response.ai.OpenClawStreamResponse;
import com.swpts.enpracticebe.dto.response.dictionary.ExampleSentence;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.util.AuthUtil;
import com.swpts.enpracticebe.util.JsonUtil;
import com.swpts.enpracticebe.util.PromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
@Service
public class OpenClawServiceImpl implements OpenClawService {
    private static final String OPENCLAW_COMPLETION_URI = "/v1/chat/completions";
    private static final String STREAM_DONE_MARKER = "[DONE]";
    private static final String REALTIME_CHAT_ERROR_MESSAGE = "Xin lỗi nha, hiện tại tôi không thể trả lời tin nhắn của bạn";

    private final WebClient webClient;
    private final AuthUtil authUtil;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<UUID, Disposable> activeStreams = new ConcurrentHashMap<>();

    public OpenClawServiceImpl(@Qualifier("openClawWebClient") WebClient webClient,
                               AuthUtil authUtil,
                               ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.authUtil = authUtil;
        this.objectMapper = objectMapper;
    }

    private OpenClawRequest getSystemRequest(String prompt) {
        OpenClawRequest request = new OpenClawRequest();
        request.setModel("openclaw");
        request.setUser("en-practice-system-operations");

        OpenClawRequest.Message msg = new OpenClawRequest.Message();
        msg.setRole("user");
        msg.setContent(prompt);
        request.setMessages(List.of(msg));

        return request;
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
                .uri(OPENCLAW_COMPLETION_URI)
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

            OpenClawRequest request = getSystemRequest(prompt);
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

    @Override
    public void streamAi(String prompt, UUID userId, Consumer<AiChatStreamResponse> eventConsumer) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(eventConsumer, "eventConsumer must not be null");

        cancelActiveStream(userId);

        String requestId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        StringBuilder accumulatedContent = new StringBuilder();

        eventConsumer.accept(AiChatStreamResponse.start(requestId, messageId));

        OpenClawRequest request = getOpenClawRequest(prompt, userId);
        request.setStream(true);

        AtomicReference<Disposable> streamRef = new AtomicReference<>();

        Flux<String> contentFlux = webClient.post()
                .uri(OPENCLAW_COMPLETION_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                })
                .map(ServerSentEvent::data)
                .filter(Objects::nonNull)
                .takeUntil(STREAM_DONE_MARKER::equals)
                .filter(data -> !STREAM_DONE_MARKER.equals(data))
                .map(this::parseStreamChunk)
                .handle((chunk, sink) -> {
                    String content = extractDeltaContent(chunk);
                    if (content != null && !content.isEmpty()) {
                        sink.next(content);
                    }
                });

        Disposable disposable = contentFlux
                .publishOn(Schedulers.boundedElastic())
                .subscribe(content -> {
                            accumulatedContent.append(content);
                            eventConsumer.accept(AiChatStreamResponse.delta(requestId, messageId, content));
                        }, error -> {
                            clearActiveStream(userId, streamRef.get());
                            log.error("Error streaming realtime chat for user {}: {}", userId, error.getMessage(), error);
                            eventConsumer.accept(AiChatStreamResponse.error(requestId, messageId, REALTIME_CHAT_ERROR_MESSAGE));
                        }, () -> {
                            clearActiveStream(userId, streamRef.get());
                            eventConsumer.accept(AiChatStreamResponse.complete(
                                    requestId,
                                    messageId,
                                    accumulatedContent.toString()));
                            log.info("Completed realtime chat stream for user {}", userId);
                        }
                );

        streamRef.set(disposable);
        activeStreams.put(userId, disposable);
    }

    @Override
    public AiAskResponse systemCallAi(String prompt) {
        String response = getOpenClawResponse(getSystemRequest(prompt));

        return AiAskResponse.builder()
                .answer(response)
                .build();
    }

    private OpenClawStreamResponse parseStreamChunk(String rawChunk) {
        try {
            return objectMapper.readValue(rawChunk, OpenClawStreamResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenClaw stream chunk", e);
        }
    }

    private String extractDeltaContent(OpenClawStreamResponse chunk) {
        if (chunk == null || chunk.getChoices() == null) {
            return null;
        }

        StringBuilder delta = new StringBuilder();
        for (OpenClawStreamResponse.Choice choice : chunk.getChoices()) {
            if (choice == null || choice.getDelta() == null) {
                continue;
            }
            String content = choice.getDelta().getContent();
            if (content != null) {
                delta.append(content);
            }
        }

        return delta.isEmpty() ? null : delta.toString();
    }

    private void cancelActiveStream(UUID userId) {
        Disposable previous = activeStreams.remove(userId);
        if (previous != null && !previous.isDisposed()) {
            previous.dispose();
            log.info("Cancelled previous realtime chat stream for user {}", userId);
        }
    }

    private void clearActiveStream(UUID userId, Disposable disposable) {
        if (disposable == null) {
            return;
        }

        activeStreams.computeIfPresent(userId, (key, current) -> current == disposable ? null : current);
    }
}
