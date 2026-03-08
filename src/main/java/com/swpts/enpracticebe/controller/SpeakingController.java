package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.speaking.SpeakingTopicFilterRequest;
import com.swpts.enpracticebe.dto.request.speaking.SubmitSpeakingRequest;
import com.swpts.enpracticebe.dto.request.speaking.SubmitTurnRequest;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.speaking.*;
import com.swpts.enpracticebe.service.ConversationSpeakingService;
import com.swpts.enpracticebe.service.FileService;
import com.swpts.enpracticebe.service.SpeakingService;
import com.swpts.enpracticebe.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/speaking")
public class SpeakingController {

    private final SpeakingService speakingService;
    private final FileService fileService;
    private final ConversationSpeakingService conversationService;
    private final AuthUtil authUtil;

    public SpeakingController(SpeakingService speakingService, FileService fileService,
                              ConversationSpeakingService conversationService, AuthUtil authUtil) {
        this.speakingService = speakingService;
        this.fileService = fileService;
        this.conversationService = conversationService;
        this.authUtil = authUtil;
    }

    @GetMapping("/topics")
    public ResponseEntity<DefaultResponse<PageResponse<SpeakingTopicListResponse>>> getTopics(
            SpeakingTopicFilterRequest request) {
        PageResponse<SpeakingTopicListResponse> result = speakingService.getTopics(request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @GetMapping("/topics/{id}")
    public ResponseEntity<DefaultResponse<SpeakingTopicResponse>> getTopicDetail(@PathVariable UUID id) {
        SpeakingTopicResponse result = speakingService.getTopicDetail(id);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @PostMapping("/topics/{id}/submit")
    public ResponseEntity<DefaultResponse<SpeakingAttemptResponse>> submitAttempt(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitSpeakingRequest request) {
        SpeakingAttemptResponse result = speakingService.submitAttempt(id, request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @GetMapping("/attempts")
    public ResponseEntity<DefaultResponse<PageResponse<SpeakingAttemptResponse>>> getAttemptHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<SpeakingAttemptResponse> result = speakingService.getAttemptHistory(page, size);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @GetMapping("/attempts/{id}")
    public ResponseEntity<DefaultResponse<SpeakingAttemptResponse>> getAttemptDetail(@PathVariable UUID id) {
        SpeakingAttemptResponse result = speakingService.getAttemptDetail(id);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @PostMapping("/upload-audio")
    public ResponseEntity<DefaultResponse<String>> uploadAudio(@RequestParam("file") MultipartFile file) {
        String audioUrl = fileService.uploadAudio(file);
        return ResponseEntity.ok(DefaultResponse.success(audioUrl));
    }

    // === Conversation endpoints ===

    @PostMapping("/conversations/start")
    public ResponseEntity<DefaultResponse<NextQuestionResponse>> startConversation(
            @RequestParam UUID topicId) {
        NextQuestionResponse result = conversationService.startConversation(topicId, authUtil.getUserId());
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @PostMapping("/conversations/{id}/turn")
    public ResponseEntity<DefaultResponse<NextQuestionResponse>> submitTurn(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitTurnRequest request) {
        NextQuestionResponse result = conversationService.submitTurn(id, request, authUtil.getUserId());
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<DefaultResponse<ConversationResponse>> getConversation(@PathVariable UUID id) {
        ConversationResponse result = conversationService.getConversation(id, authUtil.getUserId());
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @GetMapping("/conversations")
    public ResponseEntity<DefaultResponse<PageResponse<ConversationResponse>>> getConversationHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ConversationResponse> result = conversationService.getConversationHistory(page, size, authUtil.getUserId());
        return ResponseEntity.ok(DefaultResponse.success(result));
    }
}
