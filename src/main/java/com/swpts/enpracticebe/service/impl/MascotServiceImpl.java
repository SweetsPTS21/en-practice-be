package com.swpts.enpracticebe.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.response.dashboard.UserPerformanceProfile;
import com.swpts.enpracticebe.dto.response.mascot.MascotMessageDto;
import com.swpts.enpracticebe.dto.response.mascot.MascotResponse;
import com.swpts.enpracticebe.entity.MascotMessage;
import com.swpts.enpracticebe.repository.IeltsTestAttemptRepository;
import com.swpts.enpracticebe.repository.MascotMessageRepository;
import com.swpts.enpracticebe.repository.SpeakingAttemptRepository;
import com.swpts.enpracticebe.repository.VocabularyRecordRepository;
import com.swpts.enpracticebe.repository.WritingSubmissionRepository;
import com.swpts.enpracticebe.service.MascotService;
import com.swpts.enpracticebe.service.OpenClawService;
import com.swpts.enpracticebe.service.UserStatsAggregatorService;
import com.swpts.enpracticebe.util.AuthUtil;
import com.swpts.enpracticebe.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MascotServiceImpl implements MascotService {

    private final AuthUtil authUtil;
    private final MascotMessageRepository mascotMessageRepository;
    private final UserStatsAggregatorService userStatsAggregatorService;
    private final OpenClawService openClawService;
    private final ObjectMapper objectMapper;
    private final VocabularyRecordRepository vocabularyRecordRepository;
    private final IeltsTestAttemptRepository ieltsTestAttemptRepository;
    private final SpeakingAttemptRepository speakingAttemptRepository;
    private final WritingSubmissionRepository writingSubmissionRepository;

    @Override
    public MascotResponse getMascotData() {
        UUID userId = authUtil.getUserId();

        // Compute streak for mood/level context
        int streakDays = computeStreakDays(userId);
        String level = computeLevel(userId);
        String mood = computeMood(streakDays);

        // Load pre-computed messages or use defaults
        Optional<MascotMessage> saved = mascotMessageRepository.findByUserId(userId);

        List<MascotMessageDto> messages;
        if (saved.isPresent() && saved.get().getMessages() != null && !saved.get().getMessages().isEmpty()) {
            messages = saved.get().getMessages().stream()
                    .map(item -> MascotMessageDto.builder()
                            .text(item.getText())
                            .type(item.getType())
                            .mood(item.getMood())
                            .build())
                    .collect(Collectors.toList());
        } else {
            messages = getDefaultMessages(streakDays);
        }

        return MascotResponse.builder()
                .displayName("Lexi")
                .level(level)
                .mood(mood)
                .streakDays(streakDays)
                .messages(messages)
                .build();
    }

    @Override
    public void computeMascotMessages(UUID userId) {
        try {
            UserPerformanceProfile profile = userStatsAggregatorService.buildPerformanceProfile(userId);
            int streakDays = computeStreakDays(userId);
            List<String> weakSkills = userStatsAggregatorService.getWeakSkillsFromProfile(profile);

            String prompt = buildMascotPrompt(profile, weakSkills, streakDays);
            String answer = openClawService.askAi(prompt, userId).getAnswer();
            List<MascotMessage.MessageItem> items = parseAiMessages(answer);

            if (items == null || items.isEmpty()) {
                items = getDefaultMessageItems(streakDays);
            }

            MascotMessage entity = mascotMessageRepository.findByUserId(userId)
                    .orElse(MascotMessage.builder().userId(userId).build());
            entity.setMessages(items);
            entity.setComputedAt(Instant.now());
            mascotMessageRepository.save(entity);

            log.info("Computed mascot messages for user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to compute mascot messages for user {}: {}", userId, e.getMessage());
        }
    }

    private String buildMascotPrompt(UserPerformanceProfile profile, List<String> weakSkills, int streak) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are Lexi, a friendly IELTS tutor owl mascot. Generate 5 short, personalized encouragement messages for a student.\n\n");
        sb.append("Student context:\n");
        sb.append("- Current streak: ").append(streak).append(" days\n");
        sb.append("- Weak skills: ").append(String.join(", ", weakSkills)).append("\n");
        sb.append("- Total IELTS attempts: ").append(profile.getTotalIeltsAttempts()).append("\n");
        sb.append("- Total speaking attempts: ").append(profile.getTotalSpeakingAttempts()).append("\n");
        if (profile.getOverallIeltsBand() != null) {
            sb.append("- Current band score: ").append(profile.getOverallIeltsBand()).append("\n");
        }
        sb.append("\nReturn ONLY a JSON array with exactly 5 objects. Each object has:\n");
        sb.append("- \"text\": short encouraging message (max 80 chars), in English, friendly and witty\n");
        sb.append("- \"type\": one of ENCOURAGEMENT, TIP, STREAK, COMEBACK, CELEBRATE\n");
        sb.append("- \"mood\": one of idle, happy, excited, thinking, celebrate, explain\n\n");
        sb.append("Make messages specific, not generic. Reference their weak skills. Be like an IELTS coach, not too childish.");
        return sb.toString();
    }

    private List<MascotMessage.MessageItem> parseAiMessages(String content) {
        try {
            String json = content.trim();
            if (json.startsWith("```json")) json = json.substring(7);
            else if (json.startsWith("```")) json = json.substring(3);
            if (json.endsWith("```")) json = json.substring(0, json.length() - 3);
            json = json.trim();

            List<Map<String, String>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream()
                    .map(m -> MascotMessage.MessageItem.builder()
                            .text(m.getOrDefault("text", "Keep going!"))
                            .type(m.getOrDefault("type", "ENCOURAGEMENT"))
                            .mood(m.getOrDefault("mood", "happy"))
                            .build())
                    .limit(5)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to parse mascot AI messages: {}", e.getMessage());
            return null;
        }
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

    private String computeLevel(UUID userId) {
        try {
            long totalActivities = ieltsTestAttemptRepository.countByUserId(userId)
                    + speakingAttemptRepository.countByUserId(userId)
                    + writingSubmissionRepository.countByUserId(userId);
            if (totalActivities >= 50) return "master";
            if (totalActivities >= 20) return "advanced";
            if (totalActivities >= 5) return "intermediate";
            return "beginner";
        } catch (Exception e) {
            return "beginner";
        }
    }

    private String computeMood(int streakDays) {
        if (streakDays >= 7) return "excited";
        if (streakDays >= 3) return "happy";
        if (streakDays == 0) return "idle";
        return "happy";
    }

    private List<MascotMessageDto> getDefaultMessages(int streakDays) {
        List<MascotMessageDto> msgs = new ArrayList<>();
        if (streakDays == 0) {
            msgs.add(MascotMessageDto.builder().text("Hey, ready for another practice? Let's get started!").type("COMEBACK").mood("idle").build());
        } else if (streakDays >= 7) {
            msgs.add(MascotMessageDto.builder().text("🔥 " + streakDays + " day streak! You're absolutely crushing it!").type("STREAK").mood("excited").build());
        }
        msgs.add(MascotMessageDto.builder().text("Focus on paraphrasing — it's the key to Band 7+ reading!").type("TIP").mood("explain").build());
        msgs.add(MascotMessageDto.builder().text("Nice progress! Each session brings you closer to your target.").type("ENCOURAGEMENT").mood("happy").build());
        msgs.add(MascotMessageDto.builder().text("Try speaking for 2 minutes without pausing — fluency matters!").type("TIP").mood("thinking").build());
        msgs.add(MascotMessageDto.builder().text("Consistency beats intensity. 20 mins daily > 3 hours once a week.").type("ENCOURAGEMENT").mood("explain").build());
        return msgs;
    }

    private List<MascotMessage.MessageItem> getDefaultMessageItems(int streakDays) {
        return getDefaultMessages(streakDays).stream()
                .map(d -> MascotMessage.MessageItem.builder()
                        .text(d.getText()).type(d.getType()).mood(d.getMood()).build())
                .collect(Collectors.toList());
    }
}
