package com.swpts.enpracticebe.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ListRecordRequest extends PageRequest {
    private String englishWord;
    private Boolean isCorrect;
    private Instant from;
    private Instant to;
}
