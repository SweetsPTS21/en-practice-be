package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.speaking.StartCustomConversationRequest;
import com.swpts.enpracticebe.dto.request.speaking.SubmitCustomConversationTurnRequest;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.speaking.CustomConversationResponse;
import com.swpts.enpracticebe.dto.response.speaking.CustomConversationStepResponse;
import com.swpts.enpracticebe.service.CustomConversationSpeakingService;
import com.swpts.enpracticebe.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/custom-speaking-conversations")
@RequiredArgsConstructor
public class CustomSpeakingConversationController {

    private final CustomConversationSpeakingService customConversationSpeakingService;
    private final AuthUtil authUtil;

    @PostMapping("/start")
    public ResponseEntity<DefaultResponse<CustomConversationStepResponse>> startConversation(
            @Valid @RequestBody StartCustomConversationRequest request) {
        CustomConversationStepResponse result =
                customConversationSpeakingService.startConversation(request, authUtil.getUserId());
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @PostMapping("/{id}/turn")
    public ResponseEntity<DefaultResponse<CustomConversationStepResponse>> submitTurn(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitCustomConversationTurnRequest request) {
        CustomConversationStepResponse result =
                customConversationSpeakingService.submitTurn(id, request, authUtil.getUserId());
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<DefaultResponse<CustomConversationStepResponse>> finishConversation(@PathVariable UUID id) {
        CustomConversationStepResponse result =
                customConversationSpeakingService.finishConversation(id, authUtil.getUserId());
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<CustomConversationResponse>> getConversation(@PathVariable UUID id) {
        CustomConversationResponse result =
                customConversationSpeakingService.getConversation(id, authUtil.getUserId());
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<DefaultResponse<PageResponse<CustomConversationResponse>>> getConversationHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<CustomConversationResponse> result =
                customConversationSpeakingService.getConversationHistory(page, size, authUtil.getUserId());
        return ResponseEntity.ok(DefaultResponse.success(result));
    }
}
