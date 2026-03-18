package com.swpts.enpracticebe.scheduler;

import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.service.SmartReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartReminderScheduler {

    private final UserRepository userRepository;
    private final SmartReminderService smartReminderService;

    /**
     * Runs every 6 hours to pre-compute AI smart reminders for all active users.
     * Prevents slow AI calls from blocking the Dashboard API.
     */
    @Scheduled(cron = "0 30 */6 * * *")
    public void computeSmartRemindersForAllUsers() {
        log.info("Starting scheduled smart reminder computation...");

        List<UUID> activeUserIds = userRepository.findAllActiveUserIds();

        int success = 0;
        int failed = 0;
        for (UUID userId : activeUserIds) {
            try {
                smartReminderService.computeSmartReminder(userId);
                success++;
            } catch (Exception e) {
                failed++;
                log.warn("Failed to compute smart reminder for user {}: {}", userId, e.getMessage());
            }
        }
        log.info("Smart reminder computation completed. Success: {}, Failed: {}", success, failed);
    }
}
