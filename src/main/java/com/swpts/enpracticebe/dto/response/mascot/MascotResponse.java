package com.swpts.enpracticebe.dto.response.mascot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MascotResponse {
    /** beginner | intermediate | advanced | master */
    private String level;
    /** idle | happy | excited | thinking | celebrate | explain | sleepy */
    private String mood;
    private String displayName;
    private int streakDays;
    private List<MascotMessageDto> messages;
}
