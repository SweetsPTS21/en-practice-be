package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.profile.UpdateUserProfileRequest;
import com.swpts.enpracticebe.dto.response.DefaultResponse;
import com.swpts.enpracticebe.dto.response.profile.UserProfileResponse;
import com.swpts.enpracticebe.dto.response.profile.UserProfileSummaryResponse;
import com.swpts.enpracticebe.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/summary")
    public ResponseEntity<DefaultResponse<UserProfileSummaryResponse>> getProfileSummary() {
        return ResponseEntity.ok(DefaultResponse.success(userProfileService.getCurrentUserProfileSummary()));
    }

    @GetMapping
    public ResponseEntity<DefaultResponse<UserProfileResponse>> getProfile() {
        return ResponseEntity.ok(DefaultResponse.success(userProfileService.getCurrentUserProfile()));
    }

    @PutMapping
    public ResponseEntity<DefaultResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(DefaultResponse.success(userProfileService.updateCurrentUserProfile(request)));
    }
}
