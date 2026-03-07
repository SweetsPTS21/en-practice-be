package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.SubmitSpeakingRequest;
import com.swpts.enpracticebe.dto.request.SpeakingTopicFilterRequest;
import com.swpts.enpracticebe.dto.response.*;
import com.swpts.enpracticebe.service.FileService;
import com.swpts.enpracticebe.service.SpeakingService;
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

    public SpeakingController(SpeakingService speakingService, FileService fileService) {
        this.speakingService = speakingService;
        this.fileService = fileService;
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
}
