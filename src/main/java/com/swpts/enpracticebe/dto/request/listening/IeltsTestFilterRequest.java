package com.swpts.enpracticebe.dto.request.listening;

import lombok.Data;

@Data
public class IeltsTestFilterRequest {
    private String skill; // LISTENING, READING (optional)
    private String difficulty; // EASY, MEDIUM, HARD (optional)
    private int page = 0;
    private int size = 10;
}
