package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.entity.UserSmartReminder;
import com.swpts.enpracticebe.repository.IeltsTestAttemptRepository;
import com.swpts.enpracticebe.repository.SpeakingAttemptRepository;
import com.swpts.enpracticebe.repository.UserSmartReminderRepository;
import com.swpts.enpracticebe.repository.VocabularyRecordRepository;
import com.swpts.enpracticebe.repository.WritingSubmissionRepository;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.service.SmartReminderService;
import com.swpts.enpracticebe.service.UserStatsAggregatorService;
import com.swpts.enpracticebe.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartReminderServiceImpl implements SmartReminderService {

    private final UserSmartReminderRepository reminderRepository;
    private final UserStatsAggregatorService userStatsAggregatorService;
    private final OpenClawService openClawService;
    private final ObjectMapper objectMapper;
    private final VocabularyRecordRepository vocabularyRecordRepository;
    private final IeltsTestAttemptRepository ieltsTestAttemptRepository;
    private final SpeakingAttemptRepository speakingAttemptRepository;
    private final WritingSubmissionRepository writingSubmissionRepository;

    @Override
    public void computeSmartReminder(UUID userId) {
        try {
            // --- Build context ---
            int streakDays = computeStreakDays(userId);
            long totalIelts = ieltsTestAttemptRepository.countByUserId(userId);
            long totalSpeaking = speakingAttemptRepository.countByUserId(userId);
            long totalWriting = writingSubmissionRepository.countByUserId(userId);

            List<String> weakSkills;
            try {
                weakSkills = userStatsAggregatorService.getWeakSkillsFromProfile(
                        userStatsAggregatorService.buildPerformanceProfile(userId));
            } catch (Exception e) {
                weakSkills = List.of();
            }

            boolean studiedToday = hasStudiedToday(userId);

            // --- Build AI prompt ---
            String prompt = buildPrompt(streakDays, studiedToday, weakSkills,
                    totalIelts, totalSpeaking, totalWriting);

            // --- Call AI ---
            String aiAnswer = openClawService.systemCallAi(prompt).getAnswer();
            UserSmartReminder.ReminderPayload payload = parseAiResponse(aiAnswer);

            if (payload == null) {
                payload = buildFallback(streakDays, studiedToday, weakSkills);
            }

            // --- Persist (upsert) ---
            UserSmartReminder entity = reminderRepository.findByUserId(userId)
                    .orElse(UserSmartReminder.builder().userId(userId).build());
            entity.setReminder(payload);
            entity.setComputedAt(Instant.now());
            reminderRepository.save(entity);

            log.info("Computed smart reminder for user {}: type={}", userId, payload.getType());
        } catch (Exception e) {
            log.warn("Failed to compute smart reminder for user {}: {}", userId, e.getMessage());
        }
    }

    private String buildPrompt(int streak, boolean studiedToday,
                               List<String> weakSkills, long ielts, long speaking, long writing) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an IELTS coach generating a short, personalized smart reminder for a student.\n\n");
        sb.append("## Student Context\n");
        sb.append("- Study streak: ").append(streak).append(" day(s)\n");
        sb.append("- Studied today: ").append(studiedToday).append("\n");
        sb.append("- Total IELTS attempts: ").append(ielts).append("\n");
        sb.append("- Total Speaking attempts: ").append(speaking).append("\n");
        sb.append("- Total Writing attempts: ").append(writing).append("\n");
        if (!weakSkills.isEmpty()) {
            sb.append("- Weak skills: ").append(String.join(", ", weakSkills)).append("\n");
        }
        sb.append("\n## Instructions\n");
        sb.append("Return ONLY a single JSON object (no markdown, no explanation):\n");
        sb.append("{\n");
        sb.append("  \"title\": \"Short motivational title (max 8 words)\",\n");
        sb.append("  \"message\": \"Personalized message referencing their context (max 20 words)\",\n");
        sb.append("  \"type\": \"WARNING | INFO | SUCCESS | TIP\",\n");
        sb.append("  \"ctaText\": \"Short call-to-action button label (max 4 words)\",\n");
        sb.append("  \"ctaPath\": \"/ielts | /speaking | /writing | /\"\n");
        sb.append("}\n\n");
        sb.append("Rules:\n");
        sb.append("- If streak == 0 and not studied today → type WARNING, motivate them to start\n");
        sb.append("- If streak >= 7 → type SUCCESS, celebrate the milestone\n");
        sb.append("- If weak skills exist → type TIP, focus on the top weak skill\n");
        sb.append("- Otherwise → type INFO, give a general study tip\n");
        sb.append("- ctaPath should match the recommended activity (e.g. /speaking if Speaking is weak)\n");
        sb.append("- Be specific, concise, and encouraging. No emojis in title.");
        return sb.toString();
    }

    private UserSmartReminder.ReminderPayload parseAiResponse(String content) {
        try {
            String json = content.trim();
            if (json.startsWith("```json")) json = json.substring(7);
            else if (json.startsWith("```")) json = json.substring(3);
            if (json.endsWith("```")) json = json.substring(0, json.length() - 3);
            json = json.trim();

            Map<String, String> map = objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructMapType(Map.class, String.class, String.class));

            return UserSmartReminder.ReminderPayload.builder()
                    .title(map.getOrDefault("title", "Keep it up!"))
                    .message(map.getOrDefault("message", "Practice makes perfect."))
                    .type(map.getOrDefault("type", "INFO"))
                    .ctaText(map.getOrDefault("ctaText", "Practice Now"))
                    .ctaPath(map.getOrDefault("ctaPath", "/ielts"))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse smart reminder AI response: {}", e.getMessage());
            return null;
        }
    }

    /** Rule-based fallback if AI fails */
    private UserSmartReminder.ReminderPayload buildFallback(int streak, boolean studiedToday,
                                                             List<String> weakSkills) {
        if (!studiedToday && streak == 0) {
            return UserSmartReminder.ReminderPayload.builder()
                    .title("Keep your streak alive!")
                    .message("You haven't practiced today. 5 minutes is all it takes.")
                    .type("WARNING")
                    .ctaText("Practice Now")
                    .ctaPath("/ielts")
                    .build();
        }
        if (streak >= 7) {
            return UserSmartReminder.ReminderPayload.builder()
                    .title(streak + "-day streak — impressive!")
                    .message("You're on a roll. Keep up the great work!")
                    .type("SUCCESS")
                    .ctaText("Continue")
                    .ctaPath("/ielts")
                    .build();
        }
        if (!weakSkills.isEmpty()) {
            String skill = weakSkills.get(0);
            String path = skill.toLowerCase().contains("speaking") ? "/speaking"
                    : skill.toLowerCase().contains("writing") ? "/writing" : "/ielts";
            return UserSmartReminder.ReminderPayload.builder()
                    .title("Focus on " + skill)
                    .message("Your " + skill + " score can improve with targeted practice today.")
                    .type("TIP")
                    .ctaText("Start Practice")
                    .ctaPath(path)
                    .build();
        }
        return UserSmartReminder.ReminderPayload.builder()
                .title("Daily tip")
                .message("Consistency beats intensity — 20 mins daily matters more than binging.")
                .type("INFO")
                .ctaText("Practice")
                .ctaPath("/ielts")
                .build();
    }

    private boolean hasStudiedToday(UUID userId) {
        Instant todayStart = DateUtil.getStartOfToday();
        Instant now = Instant.now();
        return ieltsTestAttemptRepository.existsByUserIdAndStartedAtBetween(userId, todayStart, now)
                || speakingAttemptRepository.existsByUserIdAndSubmittedAtBetween(userId, todayStart, now)
                || writingSubmissionRepository.existsByUserIdAndSubmittedAtBetween(userId, todayStart, now)
                || vocabularyRecordRepository.countByUserIdAndTestedAtAfter(userId, todayStart) > 0;
    }

    private int computeStreakDays(UUID userId) {
        try {
            List<java.sql.Date> vocabDates = vocabularyRecordRepository.findDistinctRecordDates(userId);
            LocalDate today = LocalDate.now(ZoneId.of("UTC"));
            Set<LocalDate> activityDates = new HashSet<>();
            for (java.sql.Date d : vocabDates) activityDates.add(d.toLocalDate());

            Instant sevenDaysAgo = today.minusDays(6).atStartOfDay(ZoneId.of("UTC")).toInstant();
            ieltsTestAttemptRepository.findByUserIdAndStartedAtBetween(userId, sevenDaysAgo, Instant.now())
                    .forEach(a -> activityDates.add(LocalDate.ofInstant(a.getStartedAt(), ZoneId.of("UTC"))));
            speakingAttemptRepository.findByUserIdAndSubmittedAtBetween(userId, sevenDaysAgo, Instant.now())
                    .forEach(a -> activityDates.add(LocalDate.ofInstant(a.getSubmittedAt(), ZoneId.of("UTC"))));
            writingSubmissionRepository.findByUserIdAndSubmittedAtBetween(userId, sevenDaysAgo, Instant.now())
                    .forEach(a -> activityDates.add(LocalDate.ofInstant(a.getSubmittedAt(), ZoneId.of("UTC"))));

            int streak = 0;
            LocalDate cursor = today;
            while (activityDates.contains(cursor)) { streak++; cursor = cursor.minusDays(1); }
            return streak;
        } catch (Exception e) {
            return 0;
        }
    }
}
