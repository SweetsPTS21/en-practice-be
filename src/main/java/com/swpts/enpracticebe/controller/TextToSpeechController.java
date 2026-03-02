package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.TtsRequest;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.TtsResponse;
import com.swpts.enpracticebe.service.TextToSpeechService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
@RequestMapping("api/tts")
@AllArgsConstructor
public class TextToSpeechController {

    private final TextToSpeechService textToSpeechService;

    @PostMapping("/synthesize")
    public DefaultResponse<TtsResponse> synthesize(@Valid @RequestBody TtsRequest request) {
        try {
            byte[] audioBytes = textToSpeechService.synthesize(
                    request.getText(), request.getVoiceName());

            String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

            TtsResponse response = TtsResponse.builder()
                    .text(request.getText())
                    .audioBase64(audioBase64)
                    .build();

            return DefaultResponse.success(response);
        } catch (Exception e) {
            return DefaultResponse.fail("Không thể tổng hợp giọng nói: " + e.getMessage());
        }
    }
}
