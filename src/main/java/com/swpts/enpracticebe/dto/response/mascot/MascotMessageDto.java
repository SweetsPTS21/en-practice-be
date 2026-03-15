package com.swpts.enpracticebe.dto.response.mascot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MascotMessageDto {
    private String text;
    /** ENCOURAGEMENT, TIP, STREAK, COMEBACK, CELEBRATE */
    private String type;
    /** idle, happy, excited, thinking, celebrate, explain */
    private String mood;
}
