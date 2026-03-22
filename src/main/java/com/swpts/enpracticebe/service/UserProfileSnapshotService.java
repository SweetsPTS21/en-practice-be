package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.entity.UserProfileSnapshot;

import java.util.UUID;

public interface UserProfileSnapshotService {

    UserProfileSnapshot getOrComputeSnapshot(UUID userId);

    UserProfileSnapshot computeSnapshot(UUID userId);

    void refreshAllActiveUserSnapshots();
}
