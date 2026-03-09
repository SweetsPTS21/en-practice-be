package com.swpts.enpracticebe.controller.admin;

import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AuditLogResponse;
import com.swpts.enpracticebe.service.AdminAuditLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminAuditLogQueryService adminAuditLogQueryService;

    @GetMapping
    public ResponseEntity<DefaultResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType) {

        PageResponse<AuditLogResponse> result = adminAuditLogQueryService.getAuditLogs(page, size, action, entityType);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }
}
