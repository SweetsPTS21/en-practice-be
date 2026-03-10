package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.leaderboard.XpHistoryResponse;
import com.swpts.enpracticebe.service.XpService;
import com.swpts.enpracticebe.util.AuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/xp")
public class XpController {

    private final XpService xpService;
    private final AuthUtil authUtil;

    @GetMapping("/history")
    public DefaultResponse<XpHistoryResponse> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = authUtil.getUserId();
        return DefaultResponse.success(xpService.getXpHistory(userId, page, size));
    }
}
