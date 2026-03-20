package com.swpts.enpracticebe.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VoiceName {
    US_NEURAL_J("en-US-Neural2-J", "en-US", "Giọng nam trầm mặc định"),
    US_NEURAL_I("en-US-Neural2-I", "en-US", "Giọng nam trẻ"),
    US_NEURAL_F("en-US-Neural2-F", "en-US", "Giọng nữ trẻ"),

    US_NEURAL_C("en-US-Neural2-C", "en-US", "Giọng nữ trung"),
    US_NEURAL_H("en-US-Neural2-H", "en-US", "Giọng nữ trung"),

    US_STUDIO_O("en-US-Studio-O", "en-US", "Giọng nữ tự nhiên"),
    US_CHIRP_F("en-US-Chirp-HD-F", "en-US", "Giọng nữ siêu tự nhiên");

    private final String value;
    private final String code;
    private final String description;
}
