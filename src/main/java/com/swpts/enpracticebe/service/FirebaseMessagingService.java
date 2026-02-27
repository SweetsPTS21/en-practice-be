package com.swpts.enpracticebe.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;

    public FirebaseMessagingService() {
        this.firebaseMessaging = FirebaseMessaging.getInstance();
    }

    public String sendNotification(String token, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .build();

            return firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("Error sending notification to token: {}", token, e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    public String sendNotificationWithData(String token, String title, String body, java.util.Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .putAllData(data)
                    .build();

            return firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("Error sending notification with data to token: {}", token, e);
            throw new RuntimeException("Failed to send notification with data", e);
        }
    }

    public BatchResponse sendMulticastNotification(List<String> tokens, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(notification)
                    .build();

            return firebaseMessaging.sendMulticast(message);
        } catch (FirebaseMessagingException e) {
            log.error("Error sending multicast notification", e);
            throw new RuntimeException("Failed to send multicast notification", e);
        }
    }

    public BatchResponse sendMulticastNotificationWithData(List<String> tokens, String title, String body, java.util.Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(notification)
                    .putAllData(data)
                    .build();

            return firebaseMessaging.sendMulticast(message);
        } catch (FirebaseMessagingException e) {
            log.error("Error sending multicast notification with data", e);
            throw new RuntimeException("Failed to send multicast notification with data", e);
        }
    }

    public void subscribeToTopic(List<String> tokens, String topic) {
        try {
            TopicManagementResponse response = firebaseMessaging.subscribeToTopic(tokens, topic);
            log.info("Successfully subscribed {} tokens to topic: {}", response.getSuccessCount(), topic);
        } catch (FirebaseMessagingException e) {
            log.error("Error subscribing tokens to topic: {}", topic, e);
            throw new RuntimeException("Failed to subscribe to topic", e);
        }
    }

    public void unsubscribeFromTopic(List<String> tokens, String topic) {
        try {
            TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(tokens, topic);
            log.info("Successfully unsubscribed {} tokens from topic: {}", response.getSuccessCount(), topic);
        } catch (FirebaseMessagingException e) {
            log.error("Error unsubscribing tokens from topic: {}", topic, e);
            throw new RuntimeException("Failed to unsubscribe from topic", e);
        }
    }

    public String sendTopicNotification(String topic, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(notification)
                    .build();

            return firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("Error sending topic notification to: {}", topic, e);
            throw new RuntimeException("Failed to send topic notification", e);
        }
    }
}
