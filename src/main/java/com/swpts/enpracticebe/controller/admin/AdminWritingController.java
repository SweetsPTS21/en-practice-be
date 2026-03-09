package com.swpts.enpracticebe.controller.admin;

import com.swpts.enpracticebe.dto.request.admin.CreateWritingTaskRequest;
import com.swpts.enpracticebe.dto.request.admin.UpdateWritingTaskRequest;
import com.swpts.enpracticebe.dto.request.writing.WritingTaskFilterRequest;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminWritingTaskResponse;
import com.swpts.enpracticebe.service.AdminWritingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/writing")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWritingController {

    private final AdminWritingService adminWritingService;

    public AdminWritingController(AdminWritingService adminWritingService) {
        this.adminWritingService = adminWritingService;
    }

    /**
     * GET
     * /api/admin/writing/tasks?taskType=TASK_1&difficulty=MEDIUM&isPublished=true&page=0&size=10
     */
    @GetMapping("/tasks")
    public ResponseEntity<DefaultResponse<PageResponse<AdminWritingTaskResponse>>> listTasks(
            WritingTaskFilterRequest request) {
        PageResponse<AdminWritingTaskResponse> result = adminWritingService.listTasks(request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * GET /api/admin/writing/tasks/{id}
     */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<DefaultResponse<AdminWritingTaskResponse>> getTaskDetail(
            @PathVariable UUID id) {
        AdminWritingTaskResponse result = adminWritingService.getTaskDetail(id);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * POST /api/admin/writing/tasks
     */
    @PostMapping("/tasks")
    public ResponseEntity<DefaultResponse<AdminWritingTaskResponse>> createTask(
            @Valid @RequestBody CreateWritingTaskRequest request) {
        AdminWritingTaskResponse result = adminWritingService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DefaultResponse.success(result));
    }

    /**
     * PUT /api/admin/writing/tasks/{id}
     */
    @PutMapping("/tasks/{id}")
    public ResponseEntity<DefaultResponse<AdminWritingTaskResponse>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWritingTaskRequest request) {
        AdminWritingTaskResponse result = adminWritingService.updateTask(id, request);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    /**
     * DELETE /api/admin/writing/tasks/{id}
     */
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<DefaultResponse<Void>> deleteTask(@PathVariable UUID id) {
        adminWritingService.deleteTask(id);
        return ResponseEntity.ok(DefaultResponse.success("Writing task deleted successfully"));
    }

    /**
     * PATCH /api/admin/writing/tasks/{id}/publish
     * Body: { "published": true }
     */
    @PatchMapping("/tasks/{id}/publish")
    public ResponseEntity<DefaultResponse<Void>> togglePublish(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body) {
        boolean published = body.getOrDefault("published", false);
        adminWritingService.togglePublish(id, published);
        return ResponseEntity.ok(DefaultResponse.success(
                published ? "Task published" : "Task unpublished"));
    }
}
