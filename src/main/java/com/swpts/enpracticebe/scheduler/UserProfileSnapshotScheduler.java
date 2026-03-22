package com.swpts.enpracticebe.scheduler;

import com.swpts.enpracticebe.service.UserProfileSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileSnapshotScheduler {

    private final UserProfileSnapshotService userProfileSnapshotService;

    /**
     * Refresh profile snapshots periodically so profile reads stay lightweight.
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void refreshSnapshotsEveryThirtyMinutes() {
        log.info("Starting scheduled user profile snapshot refresh...");
        userProfileSnapshotService.refreshAllActiveUserSnapshots();
        log.info("Scheduled user profile snapshot refresh completed.");
    }

    /**
     * Nightly full refresh to reconcile any stale or missed data.
     */
    @Scheduled(cron = "0 15 0 * * *")
    public void nightlyFullRefresh() {
        log.info("Starting nightly full user profile snapshot refresh...");
        userProfileSnapshotService.refreshAllActiveUserSnapshots();
        log.info("Nightly full user profile snapshot refresh completed.");
    }
}
