package com.swpts.enpracticebe.dto.request.speaking;

import com.swpts.enpracticebe.constant.CustomConversationExpertise;
import com.swpts.enpracticebe.constant.CustomConversationPersonality;
import com.swpts.enpracticebe.constant.CustomConversationStyle;
import com.swpts.enpracticebe.constant.VoiceName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartCustomConversationRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotNull(message = "Style is required")
    private CustomConversationStyle style;

    @NotNull(message = "Personality is required")
    private CustomConversationPersonality personality;

    @NotNull(message = "Expertise is required")
    private CustomConversationExpertise expertise;

    @NotNull(message = "Voice name is required")
    private VoiceName voiceName;

    @NotNull(message = "Grading option is required")
    private Boolean gradingEnabled;
}
