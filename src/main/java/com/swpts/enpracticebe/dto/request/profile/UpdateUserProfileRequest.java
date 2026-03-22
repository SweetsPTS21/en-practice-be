package com.swpts.enpracticebe.dto.request.profile;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {

    @Size(min = 1, max = 100)
    private String displayName;

    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 500)
    private String bio;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "9.0")
    private Float targetIeltsBand;

    private LocalDate targetExamDate;

    @Min(1)
    @Max(1440)
    private Integer dailyGoalMinutes;

    @Min(1)
    @Max(10000)
    private Integer weeklyWordGoal;

    @Size(max = 50)
    private String preferredSkill;
}
