package com.swpts.enpracticebe.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v2.SpeechClient;
import com.google.cloud.speech.v2.SpeechSettings;
import com.swpts.enpracticebe.constant.GoogleSttProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(GoogleSttProperties.class)
public class GoogleSpeechConfig {

    @Bean(destroyMethod = "close")
    public SpeechClient speechClient(GoogleSttProperties props) throws IOException {
        byte[] decoded = Base64.getDecoder().decode(props.getCredentialsBase64());

        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(decoded)
        );

        SpeechSettings settings = SpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .setEndpoint(props.getEndpoint())
                .build();

        return SpeechClient.create(settings);
    }
}
