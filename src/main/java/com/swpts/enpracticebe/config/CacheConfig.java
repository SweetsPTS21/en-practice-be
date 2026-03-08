package com.swpts.enpracticebe.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("presignUrl", "ieltsTestDetail",
                "adminIeltsTestList", "ieltsTestList", "adminWritingTaskList", "writingTaskDetail", "writingTaskList",
                "adminSpeakingTopicList", "speakingTopicDetail", "speakingTopicList");
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}
