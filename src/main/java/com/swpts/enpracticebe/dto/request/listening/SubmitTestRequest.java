package com.swpts.enpracticebe.dto.request.listening;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SubmitTestRequest {
    private UUID attemptId;
    private List<AnswerItem> answers;
    private Integer timeSpentSeconds;
}
