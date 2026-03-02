package com.swpts.enpracticebe.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TtsResponse {
    private String text;
    private String audioBase64;
}
