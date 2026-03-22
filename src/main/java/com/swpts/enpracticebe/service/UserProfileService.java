package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.profile.UpdateUserProfileRequest;
import com.swpts.enpracticebe.dto.response.profile.UserProfileResponse;
import com.swpts.enpracticebe.dto.response.profile.UserProfileSummaryResponse;

public interface UserProfileService {

    UserProfileSummaryResponse getCurrentUserProfileSummary();

    UserProfileResponse getCurrentUserProfile();

    UserProfileResponse updateCurrentUserProfile(UpdateUserProfileRequest request);
}
