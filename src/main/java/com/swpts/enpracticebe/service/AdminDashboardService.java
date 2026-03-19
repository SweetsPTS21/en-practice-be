package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.admin.RecentActivityRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.DashboardStatsResponse;
import com.swpts.enpracticebe.dto.response.admin.RecentActivityResponse;
import com.swpts.enpracticebe.dto.response.admin.UserActivityChartResponse;

import java.util.List;

public interface AdminDashboardService {

    DashboardStatsResponse getStats();

    DashboardStatsResponse refreshStats();

    PageResponse<RecentActivityResponse> getRecentActivities(RecentActivityRequest request);

    List<UserActivityChartResponse> getUserActivityChart(int days);
}
