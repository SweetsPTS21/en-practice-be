package com.swpts.enpracticebe.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FirebaseTokenRequest {
    @NotBlank(message = "FCM token is required")
    private String fcmToken;
}
