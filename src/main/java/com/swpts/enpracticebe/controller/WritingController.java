package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.writing.SubmitWritingRequest;
import com.swpts.enpracticebe.dto.request.writing.WritingTaskFilterRequest;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingSubmissionResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingTaskListResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingTaskResponse;
import com.swpts.enpracticebe.service.WritingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/writing")
public class WritingController {

    private final WritingService writingService;

    public WritingController(WritingService writingService) {
        this.writingService = writingService;
    }

    /**
     * GET /api/writing/tasks?taskType=TASK_1&difficulty=MEDIUM&page=0&size=10
     * List published writing tasks.
     */
    @GetMapping("/tasks")
    public ResponseEntity<DefaultResponse<PageResponse<WritingTaskListResponse>>> getWritingTasks(
            WritingTaskFilterRequest request) {
        PageResponse<WritingTaskListResponse> result = writingService.getWritingTasks(request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * GET /api/writing/tasks/{id}
     * Get writing task detail.
     */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<DefaultResponse<WritingTaskResponse>> getWritingTaskDetail(
            @PathVariable UUID id) {
        WritingTaskResponse result = writingService.getWritingTaskDetail(id);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * POST /api/writing/tasks/{id}/submit
     * Submit an essay for AI grading.
     */
    @PostMapping("/tasks/{id}/submit")
    public ResponseEntity<DefaultResponse<WritingSubmissionResponse>> submitEssay(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitWritingRequest request) {
        WritingSubmissionResponse result = writingService.submitEssay(id, request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * GET /api/writing/submissions?page=0&size=10
     * Get user's submission history.
     */
    @GetMapping("/submissions")
    public ResponseEntity<DefaultResponse<PageResponse<WritingSubmissionResponse>>> getSubmissionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<WritingSubmissionResponse> result = writingService.getSubmissionHistory(page, size);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * GET /api/writing/submissions/{id}
     * Get submission detail with grading status/results.
     */
    @GetMapping("/submissions/{id}")
    public ResponseEntity<DefaultResponse<WritingSubmissionResponse>> getSubmissionDetail(
            @PathVariable UUID id) {
        WritingSubmissionResponse result = writingService.getSubmissionDetail(id);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }
}
