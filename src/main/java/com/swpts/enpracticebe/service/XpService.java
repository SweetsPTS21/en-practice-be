package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.constant.XpSource;
import com.swpts.enpracticebe.dto.response.leaderboard.XpHistoryResponse;

import java.util.UUID;

public interface XpService {

    void earnXp(UUID userId, XpSource source, String sourceId, int amount);

    XpHistoryResponse getXpHistory(UUID userId, int page, int size);
}
