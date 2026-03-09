package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.admin.DashboardStatsResponse;
import com.swpts.enpracticebe.dto.response.admin.RecentActivityResponse;
import com.swpts.enpracticebe.dto.response.admin.UserActivityChartResponse;
import com.swpts.enpracticebe.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DefaultResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(DefaultResponse.success(adminDashboardService.getStats()));
    }

    @PostMapping("/stats/refresh")
    public ResponseEntity<DefaultResponse<DashboardStatsResponse>> refreshStats() {
        return ResponseEntity.ok(DefaultResponse.success(adminDashboardService.refreshStats()));
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<DefaultResponse<List<RecentActivityResponse>>> getRecentActivities() {
        List<RecentActivityResponse> activities = adminDashboardService.getRecentActivities();
        return ResponseEntity.ok(DefaultResponse.success(activities));
    }

    @GetMapping("/user-activity-chart")
    public ResponseEntity<DefaultResponse<List<UserActivityChartResponse>>> getUserActivityChart(
            @RequestParam(defaultValue = "7") int days) {
        List<UserActivityChartResponse> chart = adminDashboardService.getUserActivityChart(days);
        return ResponseEntity.ok(DefaultResponse.success(chart));
    }
}
