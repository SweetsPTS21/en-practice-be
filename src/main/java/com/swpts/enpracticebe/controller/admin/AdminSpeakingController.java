package com.swpts.enpracticebe.controller.admin;

import com.swpts.enpracticebe.dto.request.admin.CreateSpeakingTopicRequest;
import com.swpts.enpracticebe.dto.request.admin.UpdateSpeakingTopicRequest;
import com.swpts.enpracticebe.dto.request.speaking.SpeakingTopicFilterRequest;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminSpeakingTopicResponse;
import com.swpts.enpracticebe.service.AdminSpeakingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/speaking")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSpeakingController {

    private final AdminSpeakingService adminSpeakingService;

    public AdminSpeakingController(AdminSpeakingService adminSpeakingService) {
        this.adminSpeakingService = adminSpeakingService;
    }

    @GetMapping("/topics")
    public ResponseEntity<DefaultResponse<PageResponse<AdminSpeakingTopicResponse>>> listTopics(
            SpeakingTopicFilterRequest request) {
        PageResponse<AdminSpeakingTopicResponse> result = adminSpeakingService.listTopics(request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @GetMapping("/topics/{id}")
    public ResponseEntity<DefaultResponse<AdminSpeakingTopicResponse>> getTopicDetail(@PathVariable UUID id) {
        AdminSpeakingTopicResponse result = adminSpeakingService.getTopicDetail(id);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @PostMapping("/topics")
    public ResponseEntity<DefaultResponse<AdminSpeakingTopicResponse>> createTopic(
            @Valid @RequestBody CreateSpeakingTopicRequest request) {
        AdminSpeakingTopicResponse result = adminSpeakingService.createTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DefaultResponse.success(result));
    }

    @PutMapping("/topics/{id}")
    public ResponseEntity<DefaultResponse<AdminSpeakingTopicResponse>> updateTopic(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSpeakingTopicRequest request) {
        AdminSpeakingTopicResponse result = adminSpeakingService.updateTopic(id, request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<DefaultResponse<Void>> deleteTopic(@PathVariable UUID id) {
        adminSpeakingService.deleteTopic(id);
        return ResponseEntity.ok(DefaultResponse.success("Speaking topic deleted successfully"));
    }

    @PatchMapping("/topics/{id}/publish")
    public ResponseEntity<DefaultResponse<Void>> togglePublish(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body) {
        boolean published = body.getOrDefault("published", false);
        adminSpeakingService.togglePublish(id, published);
        return ResponseEntity.ok(DefaultResponse.success(
                published ? "Topic published" : "Topic unpublished"));
    }
}
