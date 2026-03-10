package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.constant.LeaderboardPeriod;
import com.swpts.enpracticebe.constant.LeaderboardScope;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.leaderboard.LeaderboardResponse;
import com.swpts.enpracticebe.dto.response.leaderboard.LeaderboardSummaryResponse;
import com.swpts.enpracticebe.service.LeaderboardService;
import com.swpts.enpracticebe.util.AuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final AuthUtil authUtil;

    @GetMapping
    public DefaultResponse<LeaderboardResponse> getLeaderboard(
            @RequestParam(defaultValue = "WEEKLY") LeaderboardPeriod period,
            @RequestParam(defaultValue = "GLOBAL") LeaderboardScope scope,
            @RequestParam(required = false) Float targetBand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return DefaultResponse.success(
            leaderboardService.getLeaderboard(period, scope, targetBand, page, size));
    }

    @GetMapping("/summary")
    public DefaultResponse<LeaderboardSummaryResponse> getSummary() {
        UUID userId = authUtil.getUserId();
        return DefaultResponse.success(
            leaderboardService.getLeaderboardSummary(userId, LeaderboardPeriod.WEEKLY));
    }
}
