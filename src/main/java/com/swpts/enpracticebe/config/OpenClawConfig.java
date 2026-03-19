package com.swpts.enpracticebe.config;

import com.swpts.enpracticebe.constant.OpenClawGatewayProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
@EnableConfigurationProperties(OpenClawGatewayProperties.class)
public class OpenClawConfig {

    @Bean("openClawWebClient")
    public WebClient openClawWebClient(OpenClawGatewayProperties properties) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("openclaw-pool")
                .maxConnections(properties.getMaxConnections())
                .pendingAcquireTimeout(Duration.ofMillis(properties.getPendingAcquireTimeoutMs()))
                .maxIdleTime(Duration.ofMillis(properties.getMaxIdleTimeMs()))
                .maxLifeTime(Duration.ofMillis(properties.getMaxLifeTimeMs()))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(properties.getResponseTimeoutMs()))
                .keepAlive(true)
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(properties.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)));

        log.info("OpenClaw WebClient initialized with keep-alive and a connection pool of {} connections",
                properties.getMaxConnections());

        return WebClient.builder()
                .baseUrl(properties.getUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-openclaw-agent-id", properties.getAgentId())
                .build();
    }
}
