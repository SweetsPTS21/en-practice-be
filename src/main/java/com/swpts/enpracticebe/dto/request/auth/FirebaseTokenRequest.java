package com.swpts.enpracticebe.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FirebaseTokenRequest {
    @NotBlank(message = "FCM token is required")
    private String fcmToken;
}
