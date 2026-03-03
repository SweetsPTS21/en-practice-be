package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.IeltsTestFilterRequest;
import com.swpts.enpracticebe.dto.request.SubmitTestRequest;
import com.swpts.enpracticebe.dto.response.*;
import com.swpts.enpracticebe.service.IeltsTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ielts")
public class IeltsTestController {

    private final IeltsTestService ieltsTestService;

    public IeltsTestController(IeltsTestService ieltsTestService) {
        this.ieltsTestService = ieltsTestService;
    }

    /**
     * GET /api/ielts/tests?skill=LISTENING&difficulty=MEDIUM&page=0&size=10
     * List available tests with optional filters.
     */
    @GetMapping("/tests")
    public ResponseEntity<DefaultResponse<PageResponse<IeltsTestListResponse>>> getTests(
            IeltsTestFilterRequest request) {
        PageResponse<IeltsTestListResponse> result = ieltsTestService.getTests(request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * GET /api/ielts/tests/{id}
     * Get full test detail (sections → passages → questions, without answers).
     */
    @GetMapping("/tests/{id}")
    public ResponseEntity<DefaultResponse<IeltsTestDetailResponse>> getTestDetail(
            @PathVariable UUID id) {
        IeltsTestDetailResponse result = ieltsTestService.getTestDetail(id);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * POST /api/ielts/tests/{id}/start
     * Start a new test attempt.
     */
    @PostMapping("/tests/{id}/start")
    public ResponseEntity<DefaultResponse<StartTestResponse>> startTest(
            @PathVariable UUID id) {
        StartTestResponse result = ieltsTestService.startTest(id);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * POST /api/ielts/attempts/{attemptId}/submit
     * Submit answers and get auto-graded result.
     */
    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<DefaultResponse<SubmitTestResponse>> submitTest(
            @PathVariable UUID attemptId,
            @RequestBody SubmitTestRequest request) {
        request.setAttemptId(attemptId);
        SubmitTestResponse result = ieltsTestService.submitTest(request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * GET /api/ielts/attempts?page=0&size=10
     * Get user's test attempt history.
     */
    @GetMapping("/attempts")
    public ResponseEntity<DefaultResponse<PageResponse<TestAttemptHistoryResponse>>> getAttemptHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<TestAttemptHistoryResponse> result = ieltsTestService.getAttemptHistory(page, size);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * GET /api/ielts/attempts/{id}
     * Get detailed result of a specific attempt (with correct answers).
     */
    @GetMapping("/attempts/{id}")
    public ResponseEntity<DefaultResponse<SubmitTestResponse>> getAttemptDetail(
            @PathVariable UUID id) {
        SubmitTestResponse result = ieltsTestService.getAttemptDetail(id);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }
}
