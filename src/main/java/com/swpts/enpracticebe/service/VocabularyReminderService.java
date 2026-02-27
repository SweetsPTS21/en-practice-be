package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.entity.VocabularyRecord;
import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.repository.VocabularyRecordRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class VocabularyReminderService {

    private final VocabularyRecordRepository vocabularyRecordRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    @Scheduled(cron = "0 0 10 * * ?")
    public void sendDailyVocabularyReminders() {
        log.info("Starting daily vocabulary reminder job at {}", Instant.now());
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            Instant startOfYesterday = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endOfYesterday = yesterday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
            
            List<VocabularyRecord> incorrectRecords = vocabularyRecordRepository.findByTestedAtBetweenAndIsCorrectFalse(
                startOfYesterday, endOfYesterday);
            
            if (incorrectRecords.isEmpty()) {
                log.info("No incorrect vocabulary records found for yesterday: {}", yesterday);
                return;
            }
            
            Map<UUID, List<VocabularyRecord>> userRecords = incorrectRecords.stream()
                .collect(Collectors.groupingBy(VocabularyRecord::getUserId));
            
            // Fetch all users at once to avoid repeated queries
            List<User> users = userRepository.findAllById(userRecords.keySet());
            Map<UUID, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
            
            int totalNotifications = 0;
            for (Map.Entry<UUID, List<VocabularyRecord>> entry : userRecords.entrySet()) {
                UUID userId = entry.getKey();
                List<VocabularyRecord> userIncorrectRecords = entry.getValue();
                User user = userMap.get(userId);
                
                try {
                    sendVocabularyReminder(userId, user, userIncorrectRecords, yesterday);
                    totalNotifications++;
                } catch (Exception e) {
                    log.error("Failed to send reminder to user: {} for date: {}", userId, yesterday, e);
                }
            }
            
            log.info("Completed daily vocabulary reminder job. Sent {} notifications to {} users for {}", 
                totalNotifications, userRecords.size(), yesterday);
                
        } catch (Exception e) {
            log.error("Error in daily vocabulary reminder job", e);
        }
    }
    
    private void sendVocabularyReminder(UUID userId, User user, List<VocabularyRecord> incorrectRecords, LocalDate date) {
        int incorrectCount = incorrectRecords.size();
        List<String> words = incorrectRecords.stream()
            .map(VocabularyRecord::getEnglishWord)
            .distinct()
            .limit(5)
            .collect(Collectors.toList());
        
        String userName = user.getDisplayName() != null ? user.getDisplayName() : "Bạn";
        String title = userName + " ơi,";
        String body = String.format("Hôm qua bạn có %d từ chưa chính xác, Hãy cùng ôn lại nhé!", incorrectCount);
        
        Map<String, String> data = Map.of(
            "type", "vocabulary_reminder",
            "date", date.toString(),
            "incorrect_count", String.valueOf(incorrectCount),
            "words", String.join(",", words)
        );
        
        pushNotificationService.sendNotificationToUserWithData(userId, title, body, data);
        
        log.debug("Sent vocabulary reminder to user: {} for {} incorrect words on {}", 
            userId, incorrectCount, date);
    }
}
