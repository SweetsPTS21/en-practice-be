package com.swpts.enpracticebe.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendNotificationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    @NotNull(message = "Target type is required")
    private TargetType targetType;

    private String targetRole;

    public enum TargetType {
        ALL,
        ROLE
    }
}
