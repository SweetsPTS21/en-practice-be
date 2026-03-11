package com.swpts.enpracticebe.dto.request.dictionary;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewWordRequest {
    
    @NotNull(message = "Performance score is required")
    @Min(value = 0, message = "Performance score must be between 0 and 5")
    @Max(value = 5, message = "Performance score must be between 0 and 5")
    private Integer performanceScore; // E.g., 0 = Blackout/Forgot, 5 = Perfect/Easy recall
}
