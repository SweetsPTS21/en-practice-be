package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.dashboard.DashboardResponse;
import com.swpts.enpracticebe.service.UserDashboardService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class UserDashboardController {
    
    private final UserDashboardService dashboardService;

    @GetMapping("/dashboard")
    public DefaultResponse<DashboardResponse> getDashboard() {
        return DefaultResponse.success(dashboardService.getDashboard());
    }
}
