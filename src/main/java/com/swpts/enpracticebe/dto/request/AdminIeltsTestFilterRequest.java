package com.swpts.enpracticebe.dto.request;

import lombok.Data;

@Data
public class AdminIeltsTestFilterRequest {
    private String skill;
    private String difficulty;
    private Boolean isPublished;
    private int page = 0;
    private int size = 10;
}
