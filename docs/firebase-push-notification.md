# Firebase Push Notification Implementation

## Overview
Firebase Cloud Messaging (FCM) has been successfully implemented in the Spring Boot application for sending push notifications. The implementation provides service-level methods for push notification functionality.

## Implementation Details

### 1. Dependencies Added
- Firebase Admin SDK (v9.2.0) added to `pom.xml`

### 2. Core Components Created

#### Firebase Configuration (`FirebaseConfig.java`)
- Initializes Firebase Admin SDK using service account credentials
- Reads configuration from `application.yml`
- Handles Firebase app initialization on startup

#### Firebase Messaging Service (`FirebaseMessagingService.java`)
- Low-level Firebase messaging operations:
  - Single device notification
  - Single device notification with custom data
  - Multicast notification (multiple devices)
  - Multicast notification with custom data
  - Topic-based notifications
  - Topic subscription/unsubscription

#### Push Notification Service (`PushNotificationService.java`)
- High-level business logic for push notifications:
  - Send notifications to specific users by UUID
  - Send notifications to multiple users
  - Send topic-based notifications
  - Manage topic subscriptions for users
  - Built-in error handling and logging

#### FCM Token Management
- **Entity**: `FcmToken.java` - Stores user FCM tokens (with Lombok getters/setters)
- **Repository**: `FcmTokenRepository.java` - Database operations for FCM tokens
- **Service Integration**: Updated `AuthService.java` to handle FCM token registration

### 3. Configuration

#### Application Properties (`application.yml`)
```yaml
firebase:
  config:
    file: firebase-service-account.json
  database:
    url: https://en-practice.firebaseio.com
```

### 4. Service Usage Examples

#### Send Notification to Single User
```java
@Autowired
private PushNotificationService pushNotificationService;

// Simple notification
pushNotificationService.sendNotificationToUser(userId, "Hello", "This is a test");

// With custom data
Map<String, String> data = Map.of("customKey", "customValue");
pushNotificationService.sendNotificationToUserWithData(userId, "Hello", "Test message", data);
```

#### Send Notification to Multiple Users
```java
List<UUID> userIds = List.of(userId1, userId2, userId3);

// Simple notification
BatchResponse response = pushNotificationService.sendNotificationToUsers(userIds, "Broadcast", "Message to all");

// With custom data
Map<String, String> data = Map.of("type", "announcement", "id", "123");
BatchResponse response = pushNotificationService.sendNotificationToUsersWithData(userIds, "Announcement", "Important update", data);
```

#### Topic-based Notifications
```java
// Send to topic
pushNotificationService.sendTopicNotification("general-updates", "New Update", "Check out our new features");

// Subscribe users to topic
pushNotificationService.subscribeUsersToTopic(userIds, "general-updates");

// Subscribe single user to topic
pushNotificationService.subscribeUserToTopic(userId, "general-updates");

// Unsubscribe users from topic
pushNotificationService.unsubscribeUsersFromTopic(userIds, "general-updates");
```

## Setup Requirements

### 1. Firebase Project Setup
1. Create a Firebase project at https://console.firebase.google.com
2. Enable Cloud Messaging API
3. Download service account key JSON file
4. Place the JSON file in `src/main/resources/firebase-service-account.json`

### 2. Update Configuration
1. Update `application.yml` with your Firebase project ID
2. Ensure the service account JSON file is correctly named and placed

### 3. Database Migration
The `fcm_tokens` table will be automatically created by Hibernate. The table structure:
```sql
CREATE TABLE fcm_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL
);
```

## Security
- FCM token registration requires authenticated user
- Service methods can be called from anywhere in your application with proper access control
- Tokens are associated with user IDs for proper targeting

## Error Handling
- Comprehensive error handling with detailed logging
- Service methods throw RuntimeException with descriptive messages
- Graceful handling of invalid/expired FCM tokens
- Batch response tracking for multicast operations

## Features
- ✅ Single device notifications
- ✅ Multiple device notifications (multicast)
- ✅ Topic-based notifications
- ✅ Custom data payload support
- ✅ Token management
- ✅ Topic subscription management
- ✅ Service-level API (no public REST endpoints)
- ✅ Comprehensive error handling and logging
- ✅ UUID-based user identification
