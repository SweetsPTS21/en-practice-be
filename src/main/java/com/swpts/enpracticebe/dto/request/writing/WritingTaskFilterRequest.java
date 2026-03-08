package com.swpts.enpracticebe.dto.request.writing;

import lombok.Data;

@Data
public class WritingTaskFilterRequest {
    private String taskType;
    private String difficulty;
    private Boolean isPublished;
    private int page = 0;
    private int size = 10;
}
