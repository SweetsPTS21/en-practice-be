package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.response.profile.LevelInfoResponse;

public interface LevelService {

    LevelInfoResponse calculateLevelInfo(Integer totalXp);
}
