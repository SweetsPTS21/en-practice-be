package com.swpts.enpracticebe.dto.request;

import lombok.Data;

@Data
public class SpeakingTopicFilterRequest {
    private String part;
    private String difficulty;
    private Boolean isPublished;
    private int page = 0;
    private int size = 10;
}
