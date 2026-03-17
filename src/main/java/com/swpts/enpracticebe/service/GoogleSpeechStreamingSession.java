package com.swpts.enpracticebe.service;

import com.google.api.gax.rpc.BidiStream;
import com.google.cloud.speech.v2.*;
import com.swpts.enpracticebe.constant.GoogleSttProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class GoogleSpeechStreamingSession {
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final SpeechClient speechClient;
    private final GoogleSttProperties props;
    private final WebSocketSession webSocketSession;
    private BidiStream<StreamingRecognizeRequest, StreamingRecognizeResponse> bidiStream;

    public synchronized void start() {
        if (started.get()) {
            return;
        }

        bidiStream = speechClient.streamingRecognizeCallable().call();

        RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                .setExplicitDecodingConfig(ExplicitDecodingConfig.newBuilder()
                        .setEncoding(ExplicitDecodingConfig.AudioEncoding.LINEAR16)
                        .setSampleRateHertz(16000)
                        .setAudioChannelCount(1)
                        .build())
                .addAllLanguageCodes(props.getLanguageCodes())
                .setModel(props.getModel())
                .build();

        StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(recognitionConfig)
                .setStreamingFeatures(
                        com.google.cloud.speech.v2.StreamingRecognitionFeatures.newBuilder()
                                .setInterimResults(props.isInterimResults())
                                .build()
                )
                .build();

        StreamingRecognizeRequest configRequest = StreamingRecognizeRequest.newBuilder()
                .setRecognizer(props.recognizerPath())
                .setStreamingConfig(streamingConfig)
                .build();

        bidiStream.send(configRequest);
        started.set(true);
    }

    public synchronized void sendAudio(byte[] audioBytes) {
        if (!started.get()) {
            start();
        }

        StreamingRecognizeRequest audioRequest = StreamingRecognizeRequest.newBuilder()
                .setAudio(com.google.protobuf.ByteString.copyFrom(audioBytes))
                .build();

        bidiStream.send(audioRequest);
    }

    public void pollResponses() {
        new Thread(() -> {
            try {
                for (StreamingRecognizeResponse response : bidiStream) {
                    for (StreamingRecognitionResult result : response.getResultsList()) {
                        if (result.getAlternativesCount() == 0) {
                            continue;
                        }

                        String transcript = result.getAlternatives(0).getTranscript();
                        boolean isFinal = result.getIsFinal();

                        String payload = """
                                {
                                  "type":"transcript",
                                  "final":%s,
                                  "text":%s
                                }
                                """.formatted(
                                isFinal,
                                toJsonString(transcript)
                        );

                        synchronized (webSocketSession) {
                            if (webSocketSession.isOpen()) {
                                webSocketSession.sendMessage(new TextMessage(payload));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                sendError("stt_stream_error", e.getMessage());
            }
        }, "google-stt-response-" + webSocketSession.getId()).start();
    }

    public synchronized void finish() {
        try {
            if (bidiStream != null) {
                bidiStream.closeSend();
            }
        } catch (Exception ignored) {
        }
    }

    private void sendError(String code, String message) {
        try {
            String payload = """
                    {
                      "type":"error",
                      "code":%s,
                      "message":%s
                    }
                    """.formatted(toJsonString(code), toJsonString(message == null ? "" : message));

            synchronized (webSocketSession) {
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(new TextMessage(payload));
                }
            }
        } catch (IOException ignored) {
        }
    }

    private String toJsonString(String value) {
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return "\"" + escaped + "\"";
    }
}
