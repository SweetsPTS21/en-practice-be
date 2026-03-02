package com.swpts.enpracticebe.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class TextToSpeechConfig {

    @Bean("ttsWebClient")
    public WebClient ttsWebClient(@Value("${tts.api-key}") String apiKey) {
        log.info("Google Cloud TTS REST client initialized with API Key");
        return WebClient.builder()
                .baseUrl("https://texttospeech.googleapis.com")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("X-Goog-Api-Key", apiKey)
                .build();
    }
}
