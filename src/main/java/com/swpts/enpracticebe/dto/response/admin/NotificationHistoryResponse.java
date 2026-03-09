package com.swpts.enpracticebe.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NotificationHistoryResponse {
    private UUID id;
    private UUID adminId;
    private String adminName;
    private String title;
    private String body;
    private String targetType;
    private String targetRole;
    private Integer recipientsCount;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;
}
