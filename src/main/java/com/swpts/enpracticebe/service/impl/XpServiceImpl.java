package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.constant.Constants;
import com.swpts.enpracticebe.constant.XpSource;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.leaderboard.XpHistoryEntry;
import com.swpts.enpracticebe.dto.response.leaderboard.XpHistoryResponse;
import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.entity.UserDailyXpCap;
import com.swpts.enpracticebe.entity.UserXpLog;
import com.swpts.enpracticebe.repository.UserDailyXpCapRepository;
import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.repository.UserXpLogRepository;
import com.swpts.enpracticebe.service.XpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class XpServiceImpl implements XpService {

    private final UserXpLogRepository userXpLogRepository;
    private final UserDailyXpCapRepository userDailyXpCapRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "leaderboardSummary", key = "#userId + ':WEEKLY'"),
            @CacheEvict(value = "leaderboardSummary", key = "#userId + ':MONTHLY'"),
            @CacheEvict(value = "leaderboardSummary", key = "#userId + ':ALL_TIME'"),
            @CacheEvict(value = "userRank", allEntries = true),
            @CacheEvict(value = "xpHistory", key = "#userId + ':0:20'")
    })
    public void earnXp(UUID userId, XpSource source, String sourceId, int amount) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        
        // 1. Check daily cap
        UserDailyXpCap dailyCap = userDailyXpCapRepository.findByUserIdAndDate(userId, today)
                .orElse(UserDailyXpCap.builder().userId(userId).date(today).totalXpEarned(0).build());

        if (dailyCap.getTotalXpEarned() >= Constants.MAX_DAILY_XP && source != XpSource.BAND_INCREASE) {
            log.info("User {} reached daily XP cap", userId);
            return;
        }

        int actualAmount = amount;

        // 2. Anti-gaming check
        if (sourceId != null) {
            boolean isDuplicate = userXpLogRepository.existsByUserIdAndSourceAndSourceId(userId, source.name(), sourceId);
            if (isDuplicate) {
                if (source == XpSource.FULL_TEST_COMPLETE || source == XpSource.MINI_TEST_COMPLETE || 
                    source == XpSource.SPEAKING_PRACTICE || source == XpSource.WRITING_SUBMISSION) {
                    actualAmount = actualAmount * Constants.REPEAT_XP_PENALTY_PERCENT / 100;
                } else {
                    log.info("Duplicate XP source {} for user {}. Rejected.", sourceId, userId);
                    return; // Reject other duplicate sources
                }
            }
        }

        // Cap actualAmount if it exceeds daily max
        if (source != XpSource.BAND_INCREASE && dailyCap.getTotalXpEarned() + actualAmount > Constants.MAX_DAILY_XP) {
            actualAmount = Constants.MAX_DAILY_XP - dailyCap.getTotalXpEarned();
        }

        if (actualAmount <= 0) return;

        // 3. Insert user_xp_logs
        UserXpLog xpLog = UserXpLog.builder()
                .userId(userId)
                .source(source.name())
                .sourceId(sourceId)
                .xpAmount(actualAmount)
                .build();
        userXpLogRepository.save(xpLog);

        // 4. Update users.total_xp
        User user = userRepository.findById(userId).orElseThrow();
        user.setTotalXp((user.getTotalXp() != null ? user.getTotalXp() : 0) + actualAmount);
        userRepository.save(user);

        // 5. Update daily cap
        if (source != XpSource.BAND_INCREASE) {
            dailyCap.setTotalXpEarned(dailyCap.getTotalXpEarned() + actualAmount);
            userDailyXpCapRepository.save(dailyCap);
        }
    }

    @Override
    public XpHistoryResponse getXpHistory(UUID userId, int page, int size) {
        Page<UserXpLog> xpPage = userXpLogRepository.findByUserIdOrderByEarnedAtDesc(userId, PageRequest.of(page, size));
        
        User user = userRepository.findById(userId).orElseThrow();
        
        List<XpHistoryEntry> history = xpPage.getContent().stream()
                .map(log -> XpHistoryEntry.builder()
                        .id(log.getId())
                        .source(XpSource.valueOf(log.getSource()))
                        .description("Earned from " + log.getSource())
                        .xp(log.getXpAmount())
                        .earnedAt(log.getEarnedAt())
                        .build())
                .collect(Collectors.toList());

        return XpHistoryResponse.builder()
                .totalXP(user.getTotalXp() != null ? user.getTotalXp() : 0)
                .weeklyXP(0) // Weekly XP calculation can be added if leaderboard snapshots are queried
                .history(history)
                .page(PageResponse.builder()
                        .page(page)
                        .totalPages(xpPage.getTotalPages())
                        .totalElements(xpPage.getTotalElements())
                        .build())
                .build();
    }
}
