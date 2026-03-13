package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.service.TextToSpeechService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class TextToSpeechServiceImpl implements TextToSpeechService {

        private final WebClient ttsWebClient;
        private final String defaultVoiceName;
        private final String defaultLanguageCode;

        public TextToSpeechServiceImpl(
                        @Qualifier("ttsWebClient") WebClient ttsWebClient,
                        @Value("${tts.voice-name:en-US-Neural2-J}") String defaultVoiceName,
                        @Value("${tts.language-code:en-US}") String defaultLanguageCode) {
                this.ttsWebClient = ttsWebClient;
                this.defaultVoiceName = defaultVoiceName;
                this.defaultLanguageCode = defaultLanguageCode;
        }

        @Override
        public byte[] synthesize(String text, String voiceName) {
                String selectedVoice = (StringUtils.hasText(voiceName) ? voiceName : defaultVoiceName);

                log.debug("Synthesizing text: '{}' with voice: {}", text, selectedVoice);
                return callTtsApi(text, selectedVoice, 1.0);
        }

        @Override
        @Cacheable(value = "ttsVocabulary", key = "#word.trim().toLowerCase()")
        public byte[] synthesizeVocabulary(String word) {
                if (!StringUtils.hasText(word)) {
                        throw new IllegalArgumentException("Vocabulary word must not be empty");
                }

                String trimmedWord = word.trim();
                log.debug("Synthesizing vocabulary word: '{}'", trimmedWord);
                return callTtsApi(trimmedWord, defaultVoiceName, 0.8);
        }

        private byte[] callTtsApi(String text, String voiceName, double speakingRate) {
                Map<String, Object> input = new LinkedHashMap<>();
                input.put("text", text);

                Map<String, Object> voice = new LinkedHashMap<>();
                voice.put("languageCode", defaultLanguageCode);
                voice.put("name", voiceName);

                Map<String, Object> audioConfig = new LinkedHashMap<>();
                audioConfig.put("audioEncoding", "MP3");
                audioConfig.put("speakingRate", speakingRate);
                audioConfig.put("pitch", 0.0);

                Map<String, Object> requestBody = new LinkedHashMap<>();
                requestBody.put("input", input);
                requestBody.put("voice", voice);
                requestBody.put("audioConfig", audioConfig);

                @SuppressWarnings("unchecked")
                Map<String, Object> response = ttsWebClient.post()
                                .uri("/v1/text:synthesize")
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .block();

                if (response == null || !response.containsKey("audioContent")) {
                        throw new RuntimeException("Empty response from Google Cloud TTS");
                }

                String audioContentBase64 = (String) response.get("audioContent");
                return Base64.getDecoder().decode(audioContentBase64);
        }
}
