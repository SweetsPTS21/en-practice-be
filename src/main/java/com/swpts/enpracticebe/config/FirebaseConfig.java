package com.swpts.enpracticebe.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config.base64}")
    private String firebaseConfigBase64;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        byte[] decoded = Base64.getDecoder().decode(firebaseConfigBase64);

        GoogleCredentials credentials =
                GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);

        log.info("Firebase application has been initialized successfully");
        return app;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
