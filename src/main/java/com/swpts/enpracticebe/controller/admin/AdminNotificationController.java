package com.swpts.enpracticebe.controller.admin;

import com.swpts.enpracticebe.dto.request.admin.SendNotificationRequest;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.NotificationHistoryResponse;
import com.swpts.enpracticebe.service.AdminNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @PostMapping("/send")
    public ResponseEntity<DefaultResponse<String>> sendNotification(@RequestBody @Valid SendNotificationRequest request) {
        adminNotificationService.sendNotification(request);
        return ResponseEntity.ok(DefaultResponse.success("Notification processing started"));
    }

    @GetMapping("/history")
    public ResponseEntity<DefaultResponse<PageResponse<NotificationHistoryResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<NotificationHistoryResponse> history = adminNotificationService.getHistory(pageable);
        return ResponseEntity.ok(DefaultResponse.success(history));
    }
}
