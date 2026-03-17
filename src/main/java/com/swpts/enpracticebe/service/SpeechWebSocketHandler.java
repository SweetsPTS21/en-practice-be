package com.swpts.enpracticebe.service;

import com.google.cloud.speech.v2.SpeechClient;
import com.swpts.enpracticebe.constant.GoogleSttProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SpeechWebSocketHandler extends BinaryWebSocketHandler {

    private final SpeechClient speechClient;
    private final GoogleSttProperties props;

    private final Map<String, GoogleSpeechStreamingSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        GoogleSpeechStreamingSession sttSession =
                new GoogleSpeechStreamingSession(speechClient, props, session);

        sttSession.start();
        sttSession.pollResponses();
        sessions.put(session.getId(), sttSession);

        session.sendMessage(new TextMessage("""
                    {"type":"ready","message":"stt_connected"}
                """));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        GoogleSpeechStreamingSession sttSession = sessions.get(session.getId());
        if (sttSession == null) {
            return;
        }

        byte[] audio = new byte[message.getPayload().remaining()];
        message.getPayload().get(audio);
        sttSession.sendAudio(audio);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        GoogleSpeechStreamingSession sttSession = sessions.get(session.getId());
        if (sttSession == null) {
            return;
        }

        String payload = message.getPayload();
        if ("{\"type\":\"finish\"}".equals(payload)) {
            sttSession.finish();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        GoogleSpeechStreamingSession sttSession = sessions.remove(session.getId());
        if (sttSession != null) {
            sttSession.finish();
        }
    }
}
