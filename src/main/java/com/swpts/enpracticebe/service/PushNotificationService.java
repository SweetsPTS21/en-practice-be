package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.entity.FcmToken;
import com.swpts.enpracticebe.repository.FcmTokenRepository;
import com.google.firebase.messaging.BatchResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class PushNotificationService {

    private final FirebaseMessagingService firebaseMessagingService;
    private final FcmTokenRepository fcmTokenRepository;

    public String sendNotificationToUser(UUID userId, String title, String body) {
        return sendNotificationToUserWithData(userId, title, body, null);
    }

    public String sendNotificationToUserWithData(UUID userId, String title, String body, Map<String, String> data) {
        try {
            FcmToken fcmToken = fcmTokenRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("No FCM token found for user: " + userId));

            if (data != null && !data.isEmpty()) {
                return firebaseMessagingService.sendNotificationWithData(fcmToken.getToken(), title, body, data);
            } else {
                return firebaseMessagingService.sendNotification(fcmToken.getToken(), title, body);
            }
        } catch (Exception e) {
            log.error("Failed to send notification to user: {}", userId, e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    public BatchResponse sendNotificationToUsers(List<UUID> userIds, String title, String body) {
        return sendNotificationToUsersWithData(userIds, title, body, null);
    }

    public BatchResponse sendNotificationToUsersWithData(List<UUID> userIds, String title, String body, Map<String, String> data) {
        try {
            List<String> tokenStrings = getTokens(userIds);

            if (data != null && !data.isEmpty()) {
                return firebaseMessagingService.sendMulticastNotificationWithData(tokenStrings, title, body, data);
            } else {
                return firebaseMessagingService.sendMulticastNotification(tokenStrings, title, body);
            }
        } catch (Exception e) {
            log.error("Failed to send multicast notification to users: {}", userIds, e);
            throw new RuntimeException("Failed to send multicast notification", e);
        }
    }

    public String sendTopicNotification(String topic, String title, String body) {
        try {
            return firebaseMessagingService.sendTopicNotification(topic, title, body);
        } catch (Exception e) {
            log.error("Failed to send topic notification to: {}", topic, e);
            throw new RuntimeException("Failed to send topic notification", e);
        }
    }

    public void subscribeUsersToTopic(List<UUID> userIds, String topic) {
        try {
            List<String> tokenStrings = getTokens(userIds);

            firebaseMessagingService.subscribeToTopic(tokenStrings, topic);
            log.info("Successfully subscribed {} users to topic: {}", userIds.size(), topic);
        } catch (Exception e) {
            log.error("Failed to subscribe users to topic: {}", topic, e);
            throw new RuntimeException("Failed to subscribe to topic", e);
        }
    }

    public void unsubscribeUsersFromTopic(List<UUID> userIds, String topic) {
        try {
            List<String> tokenStrings = getTokens(userIds);

            firebaseMessagingService.unsubscribeFromTopic(tokenStrings, topic);
            log.info("Successfully unsubscribed {} users from topic: {}", userIds.size(), topic);
        } catch (Exception e) {
            log.error("Failed to unsubscribe users from topic: {}", topic, e);
            throw new RuntimeException("Failed to unsubscribe from topic", e);
        }
    }

    private List<String> getTokens(List<UUID> userIds) {
        List<FcmToken> tokens = fcmTokenRepository.findByUserIdIn(userIds);
        if (tokens.isEmpty()) {
            log.warn("No FCM tokens found for users: {}", userIds);
            throw new IllegalArgumentException("No FCM tokens found for the specified users");
        }

        return tokens.stream()
                .map(FcmToken::getToken)
                .collect(Collectors.toList());
    }

    public void subscribeUserToTopic(UUID userId, String topic) {
        subscribeUsersToTopic(List.of(userId), topic);
    }

    public void unsubscribeUserFromTopic(UUID userId, String topic) {
        unsubscribeUsersFromTopic(List.of(userId), topic);
    }
}
