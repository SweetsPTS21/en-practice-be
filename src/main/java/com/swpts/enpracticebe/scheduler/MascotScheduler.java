package com.swpts.enpracticebe.scheduler;

import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.service.MascotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MascotScheduler {

    private final UserRepository userRepository;
    private final MascotService mascotService;

    /**
     * Runs every 6 hours to pre-compute AI mascot messages for all users.
     * Prevents real-time AI calls when the floating mascot panel is opened.
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void computeMascotMessagesForAllUsers() {
        log.info("Starting scheduled mascot message computation...");

        List<UUID> activeUserIds = userRepository.findAllActiveUserIds();

        int success = 0;
        int failed = 0;
        for (UUID userId : activeUserIds) {
            try {
                mascotService.computeMascotMessages(userId);
                success++;
            } catch (Exception e) {
                failed++;
                log.warn("Failed to compute mascot messages for user {}: {}", userId, e.getMessage());
            }
        }
        log.info("Mascot computation completed. Success: {}, Failed: {}", success, failed);
    }
}
