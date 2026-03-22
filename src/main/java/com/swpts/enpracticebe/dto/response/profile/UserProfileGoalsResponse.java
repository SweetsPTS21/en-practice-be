package com.swpts.enpracticebe.dto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileGoalsResponse {
    private Float targetIeltsBand;
    private LocalDate targetExamDate;
    private Integer dailyGoalMinutes;
    private Integer weeklyWordGoal;
    private String preferredSkill;
}
