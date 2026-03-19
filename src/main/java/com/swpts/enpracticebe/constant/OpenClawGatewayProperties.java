package com.swpts.enpracticebe.constant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "openclaw.gateway")
public class OpenClawGatewayProperties {

    private String url = "http://127.0.0.1:18789";
    private String token = "abc";
    private String agentId = "main";
    private int connectTimeoutMs = 3_000;
    private int responseTimeoutMs = 30_000;
    private int readTimeoutMs = 30_000;
    private int writeTimeoutMs = 30_000;
    private int maxConnections = 50;
    private int pendingAcquireTimeoutMs = 5_000;
    private int maxIdleTimeMs = 60_000;
    private int maxLifeTimeMs = 300_000;
}
