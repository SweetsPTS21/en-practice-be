package com.swpts.enpracticebe.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class CacheConfig {

    /**
     * Long-lived caches (60 min) — content lists, details, presigned URLs
     */
    private static final String[] LONG_TTL_CACHES = {
            "presignUrl", "ieltsTestDetail", "adminIeltsTestList", "ieltsTestList",
            "adminWritingTaskList", "writingTaskDetail", "writingTaskList",
            "adminSpeakingTopicList", "speakingTopicDetail", "speakingTopicList",
            "adminUserList", "adminUserDetail", "adminAuditLogList", "userWeakSkills", "xpHistory"
    };

    /**
     * Short-lived caches (5 min) — real-time dashboard data
     */
    private static final String[] SHORT_TTL_CACHES = {
            "dashboardStats", "dashboardRecentActivities", "dashboardUserActivityChart",
            "leaderboardPage", "leaderboardSummary", "userRank"
    };

    @Bean
    public CacheManager cacheManager() {
        Caffeine<Object, Object> longTtl = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats();

        Caffeine<Object, Object> shortTtl = Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats();

        List<CaffeineCache> caches = Stream.concat(
                Arrays.stream(LONG_TTL_CACHES).map(name -> new CaffeineCache(name, longTtl.build())),
                Arrays.stream(SHORT_TTL_CACHES).map(name -> new CaffeineCache(name, shortTtl.build()))
        ).collect(Collectors.toList());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(caches);
        return manager;
    }
}

