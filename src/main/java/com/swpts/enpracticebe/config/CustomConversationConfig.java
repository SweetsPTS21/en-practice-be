package com.swpts.enpracticebe.config;

import com.swpts.enpracticebe.constant.CustomConversationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CustomConversationProperties.class)
public class CustomConversationConfig {
}
