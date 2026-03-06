package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.AdminIeltsTestFilterRequest;
import com.swpts.enpracticebe.dto.request.CreateIeltsTestRequest;
import com.swpts.enpracticebe.dto.request.UpdateIeltsTestRequest;
import com.swpts.enpracticebe.dto.response.IeltsTestDetailResponse;
import com.swpts.enpracticebe.dto.response.AdminIeltsTestListResponse;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.service.AdminIeltsTestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/ielts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminIeltsTestController {

    private final AdminIeltsTestService adminIeltsTestService;

    public AdminIeltsTestController(AdminIeltsTestService adminIeltsTestService) {
        this.adminIeltsTestService = adminIeltsTestService;
    }

    /**
     * GET
     * /api/admin/ielts/tests?skill=LISTENING&difficulty=MEDIUM&isPublished=true&page=0&size=10
     */
    @GetMapping("/tests")
    public ResponseEntity<DefaultResponse<PageResponse<AdminIeltsTestListResponse>>> listTests(
            AdminIeltsTestFilterRequest request) {
        PageResponse<AdminIeltsTestListResponse> result = adminIeltsTestService.listTests(request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * GET /api/admin/ielts/tests/{id}
     */
    @GetMapping("/tests/{id}")
    public ResponseEntity<DefaultResponse<IeltsTestDetailResponse>> getTestDetail(
            @PathVariable UUID id) {
        IeltsTestDetailResponse result = adminIeltsTestService.getTestDetail(id);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * POST /api/admin/ielts/tests
     */
    @PostMapping("/tests")
    public ResponseEntity<DefaultResponse<IeltsTestDetailResponse>> createTest(
            @Valid @RequestBody CreateIeltsTestRequest request) {
        IeltsTestDetailResponse result = adminIeltsTestService.createTest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DefaultResponse.success(result));
    }

    /**
     * PUT /api/admin/ielts/tests/{id}
     */
    @PutMapping("/tests/{id}")
    public ResponseEntity<DefaultResponse<IeltsTestDetailResponse>> updateTest(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIeltsTestRequest request) {
        IeltsTestDetailResponse result = adminIeltsTestService.updateTest(id, request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * DELETE /api/admin/ielts/tests/{id}
     */
    @DeleteMapping("/tests/{id}")
    public ResponseEntity<DefaultResponse<Void>> deleteTest(@PathVariable UUID id) {
        adminIeltsTestService.deleteTest(id);
        return ResponseEntity.ok(DefaultResponse.success("Test deleted successfully"));
    }

    /**
     * PATCH /api/admin/ielts/tests/{id}/publish
     * Body: { "published": true }
     */
    @PatchMapping("/tests/{id}/publish")
    public ResponseEntity<DefaultResponse<Void>> togglePublish(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body) {
        boolean published = body.getOrDefault("published", false);
        adminIeltsTestService.togglePublish(id, published);
        return ResponseEntity.ok(DefaultResponse.success(
                published ? "Test published" : "Test unpublished"));
    }
}
