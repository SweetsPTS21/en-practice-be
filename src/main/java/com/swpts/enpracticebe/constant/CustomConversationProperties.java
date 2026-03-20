package com.swpts.enpracticebe.constant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "speaking.custom-conversation")
public class CustomConversationProperties {

    private int maxUserTurns = 100;
}
