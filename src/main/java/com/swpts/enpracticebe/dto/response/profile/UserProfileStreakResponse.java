package com.swpts.enpracticebe.dto.response.profile;

import com.swpts.enpracticebe.dto.response.dashboard.DayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileStreakResponse {
    private int currentStreak;
    private int longestStreak;
    private int activeDaysLast30;
    private List<DayStatus> heatmap;
}
