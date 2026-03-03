package com.swpts.enpracticebe.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AnswerItem {
    private UUID questionId;
    private List<String> userAnswer;
}
