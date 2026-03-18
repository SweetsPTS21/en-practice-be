package com.swpts.enpracticebe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.rpc.BidiStream;
import com.google.cloud.speech.v2.*;
import com.google.protobuf.Duration;
import com.swpts.enpracticebe.constant.GoogleSttProperties;
import com.swpts.enpracticebe.dto.speech.SpeechAnalyticsDto;
import com.swpts.enpracticebe.dto.speech.SpeechWordDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class GoogleSpeechStreamingSession {

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final SpeechClient speechClient;
    private final GoogleSttProperties props;
    private final WebSocketSession webSocketSession;
    private final SpeechAnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    /** Accumulates words from all final STT results for end-of-session summary */
    private final List<SpeechWordDto> allFinalWords = new CopyOnWriteArrayList<>();

    private BidiStream<StreamingRecognizeRequest, StreamingRecognizeResponse> bidiStream;

    public GoogleSpeechStreamingSession(SpeechClient speechClient,
                                        GoogleSttProperties props,
                                        WebSocketSession webSocketSession,
                                        SpeechAnalyticsService analyticsService,
                                        ObjectMapper objectMapper) {
        this.speechClient = speechClient;
        this.props = props;
        this.webSocketSession = webSocketSession;
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    public synchronized void start() {
        if (started.get()) return;

        bidiStream = speechClient.streamingRecognizeCallable().call();

        // ─── Recognition features — enable word timestamps + confidence ───────
        RecognitionFeatures features = RecognitionFeatures.newBuilder()
                .setEnableWordTimeOffsets(props.isEnableWordTimeOffsets())
                .setEnableWordConfidence(props.isEnableWordConfidence())
                .build();

        RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                .setExplicitDecodingConfig(ExplicitDecodingConfig.newBuilder()
                        .setEncoding(ExplicitDecodingConfig.AudioEncoding.LINEAR16)
                        .setSampleRateHertz(16000)
                        .setAudioChannelCount(1)
                        .build())
                .addAllLanguageCodes(props.getLanguageCodes())
                .setModel(props.getModel())
                .setFeatures(features)
                .build();

        StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(recognitionConfig)
                .setStreamingFeatures(
                        StreamingRecognitionFeatures.newBuilder()
                                .setInterimResults(props.isInterimResults())
                                .build())
                .build();

        StreamingRecognizeRequest configRequest = StreamingRecognizeRequest.newBuilder()
                .setRecognizer(props.recognizerPath())
                .setStreamingConfig(streamingConfig)
                .build();

        bidiStream.send(configRequest);
        started.set(true);
    }

    public synchronized void sendAudio(byte[] audioBytes) {
        if (!started.get()) start();
        bidiStream.send(StreamingRecognizeRequest.newBuilder()
                .setAudio(com.google.protobuf.ByteString.copyFrom(audioBytes))
                .build());
    }

    public void pollResponses() {
        new Thread(() -> {
            try {
                for (StreamingRecognizeResponse response : bidiStream) {
                    for (StreamingRecognitionResult result : response.getResultsList()) {
                        if (result.getAlternativesCount() == 0) continue;

                        SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                        String transcript = alternative.getTranscript();
                        boolean isFinal = result.getIsFinal();

                        if (isFinal) {
                            // Extract word-level data and accumulate
                            List<SpeechWordDto> words = extractWords(alternative);
                            allFinalWords.addAll(words);
                            sendFinalTranscript(transcript, words);
                        } else {
                            // Interim: lightweight message for real-time display
                            sendInterimTranscript(transcript);
                        }
                    }
                }

                // ─── Stream ended — compute & send speech_summary ─────────────
                if (!allFinalWords.isEmpty()) {
                    SpeechAnalyticsDto analytics = analyticsService.analyze(allFinalWords);
                    sendSpeechSummary(analytics);
                    log.info("STT session [{}]: words={}, wpm={}, pauses={}, avgConf={}",
                            webSocketSession.getId(),
                            analytics.getWordCount(),
                            analytics.getWordsPerMinute(),
                            analytics.getPauseCount(),
                            analytics.getAvgWordConfidence());
                }

            } catch (Exception e) {
                log.warn("STT stream error [{}]: {}", webSocketSession.getId(), e.getMessage());
                sendError("stt_stream_error", e.getMessage());
            }
        }, "google-stt-" + webSocketSession.getId()).start();
    }

    public synchronized void finish() {
        try {
            if (bidiStream != null) bidiStream.closeSend();
        } catch (Exception ignored) {
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Extract word-level timing and confidence from a final STT alternative.
     * Google STT v2 returns Duration offsets; we convert to milliseconds.
     */
    private List<SpeechWordDto> extractWords(SpeechRecognitionAlternative alternative) {
        List<SpeechWordDto> result = new ArrayList<>();
        for (WordInfo wi : alternative.getWordsList()) {
            long startMs = durationToMs(wi.getStartOffset());
            long endMs = durationToMs(wi.getEndOffset());
            float confidence = wi.getConfidence();
            result.add(SpeechWordDto.builder()
                    .word(wi.getWord())
                    .startMs(startMs)
                    .endMs(endMs)
                    .confidence(confidence)
                    .build());
        }
        return result;
    }

    private static long durationToMs(Duration d) {
        return d.getSeconds() * 1_000L + d.getNanos() / 1_000_000L;
    }

    /** Send final transcript with word-level data */
    private void sendFinalTranscript(String transcript, List<SpeechWordDto> words) {
        try {
            double avgConf = words.isEmpty() ? 0.0
                    : words.stream().mapToDouble(SpeechWordDto::getConfidence).average().orElse(0.0);
            Map<String, Object> payload = Map.of(
                    "type", "transcript",
                    "final", true,
                    "text", transcript,
                    "words", words,
                    "avgConfidence", Math.round(avgConf * 1000.0) / 1000.0
            );
            sendJson(payload);
        } catch (Exception e) {
            log.warn("Failed to send final transcript: {}", e.getMessage());
        }
    }

    /** Send lightweight interim transcript (no word data to save bandwidth) */
    private void sendInterimTranscript(String transcript) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "transcript",
                    "final", false,
                    "text", transcript
            );
            sendJson(payload);
        } catch (Exception e) {
            log.warn("Failed to send interim transcript: {}", e.getMessage());
        }
    }

    /** Send end-of-session speech analytics summary */
    private void sendSpeechSummary(SpeechAnalyticsDto analytics) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "speech_summary",
                    "wpm", analytics.getWordsPerMinute(),
                    "wordCount", analytics.getWordCount(),
                    "pauseCount", analytics.getPauseCount(),
                    "avgPauseDurationMs", analytics.getAvgPauseDurationMs(),
                    "longPauseCount", analytics.getLongPauseCount(),
                    "fillerWordCount", analytics.getFillerWordCount(),
                    "fillerWords", analytics.getFillerWords(),
                    "avgWordConfidence", analytics.getAvgWordConfidence(),
                    "lowConfidenceWords", analytics.getLowConfidenceWords()
            );
            sendJson(payload);
        } catch (Exception e) {
            log.warn("Failed to send speech summary: {}", e.getMessage());
        }
    }

    private void sendError(String code, String message) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "error",
                    "code", code,
                    "message", message == null ? "" : message
            );
            sendJson(payload);
        } catch (Exception ignored) {
        }
    }

    private void sendJson(Object payload) throws IOException {
        String json = objectMapper.writeValueAsString(payload);
        synchronized (webSocketSession) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(json));
            }
        }
    }
}
