package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.response.admin.DashboardStatsResponse;
import com.swpts.enpracticebe.dto.response.admin.RecentActivityResponse;
import com.swpts.enpracticebe.dto.response.admin.UserActivityChartResponse;

import java.util.List;

public interface AdminDashboardService {

    DashboardStatsResponse getStats();

    DashboardStatsResponse refreshStats();

    List<RecentActivityResponse> getRecentActivities();

    List<UserActivityChartResponse> getUserActivityChart(int days);
}
