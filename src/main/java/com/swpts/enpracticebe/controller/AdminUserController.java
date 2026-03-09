package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.admin.UserFilterRequest;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminUserDetailResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminUserListResponse;
import com.swpts.enpracticebe.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<DefaultResponse<PageResponse<AdminUserListResponse>>> listUsers(
            UserFilterRequest filter) {
        PageResponse<AdminUserListResponse> result = adminUserService.listUsers(filter);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<AdminUserDetailResponse>> getUserDetail(
            @PathVariable UUID id) {
        AdminUserDetailResponse result = adminUserService.getUserDetail(id);
        return ResponseEntity.ok(DefaultResponse.success(result));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<DefaultResponse<Void>> changeRole(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String role = body.get("role");
        adminUserService.changeRole(id, role);
        return ResponseEntity.ok(DefaultResponse.success("Role updated successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DefaultResponse<Void>> toggleStatus(@PathVariable UUID id) {
        adminUserService.toggleStatus(id);
        return ResponseEntity.ok(DefaultResponse.success("Status updated successfully"));
    }
}
