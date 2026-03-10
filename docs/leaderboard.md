# Tài liệu Thiết kế Hệ thống - Leaderboard (System Design)

## 1. Tổng quan (Overview)

Hệ thống Leaderboard thuộc **Phase 3: Gamification & Social** (xem [`user_home_system_design.md`](./user_home_system_design.md)), nhằm tạo động lực cạnh tranh lành mạnh và tăng tỷ lệ quay lại ứng dụng (Retention) thông qua cơ chế **Social Proof** — người dùng thấy thứ hạng của mình so với cộng đồng và được thúc đẩy cải thiện.

### Nguyên tắc thiết kế

| Nguyên tắc | Mô tả |
|---|---|
| **Fair Ranking** | Xếp hạng công bằng, minh bạch dựa trên XP thực tế |
| **Near Real-time** | Cập nhật thứ hạng gần thời gian thực nhưng tối ưu hiệu năng qua Caffeine Cache + Scheduled Job |
| **Anti-gaming** | Chống gian lận, giới hạn XP tối đa/ngày để tránh spam |
| **Motivation Loop** | Hiển thị khoảng cách tới hạng trên để kích thích cố gắng |

### UX Flow

```
Mở Dashboard → Xem widget "Weekly Leaderboard" → Thấy thứ hạng hiện tại
→ Click "View Full Leaderboard" → Xem bảng xếp hạng đầy đủ (tuần/tháng/all-time)
→ Thấy khoảng cách XP tới hạng trên → Quay lại luyện tập
```

---

## 2. Phân tích Kiến trúc Hiện tại của Dự án

> Phần này tóm tắt kiến trúc backend hiện tại để đảm bảo Leaderboard tích hợp liền mạch.

### 2.1. Technology Stack

| Thành phần | Công nghệ |
|---|---|
| Framework | Spring Boot (Java 17+, Maven) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Cache | **Caffeine** (in-process, `SimpleCacheManager` với 2 tier: long TTL 60 min, short TTL 5 min) |
| Auth | JWT (stateless, `JwtFilter` + Spring Security) |
| Scheduling | `@EnableScheduling` + `ThreadPoolTaskScheduler` (pool size 5) |
| Async | `CompletableFuture` (đã dùng trong `UserDashboardServiceImpl`) |
| API Pattern | RESTful, wrap response bằng `DefaultResponse<T>` |
| File Storage | Backblaze S3 |
| Notifications | Firebase Cloud Messaging |

### 2.2. Package Structure (liên quan)

```
com.swpts.enpracticebe
├── config/          → CacheConfig, SchedulingConfig, SecurityConfig, ...
├── constant/        → Role, Constants, S3Properties
├── controller/      → RESTful controllers (UserDashboardController, ...)
│   └── admin/       → Admin-only controllers
├── dto/
│   ├── request/     → Request DTOs
│   └── response/    → Response DTOs (DefaultResponse, PageResponse, dashboard/*)
├── entity/          → JPA Entities (User, IeltsTestAttempt, UserActivityLog, ...)
├── exception/       → Custom exceptions
├── mapper/          → Object mappers
├── repository/      → Spring Data JPA Repositories
├── security/        → JwtFilter, JwtUtil, ...
├── service/         → Service interfaces
│   └── impl/        → Service implementations
└── util/            → AuthUtil, DateUtil, ...
```

### 2.3. Các Pattern đang sử dụng (cần tuân thủ)

1. **Interface + Impl pattern:** Mọi service đều có interface (`UserDashboardService`) và implementation (`UserDashboardServiceImpl`).
2. **Caffeine Cache 2-tier:** Cache config chia 2 nhóm TTL (xem `CacheConfig.java`). Leaderboard cần đăng ký cache name vào đây.
3. **`DefaultResponse<T>` wrapper:** Tất cả API response đều wrap bằng `DefaultResponse.success(data)`.
4. **UUID primary key:** Tất cả entity dùng `UUID` làm PK, generate bởi `GenerationType.UUID`.
5. **Scheduled Jobs:** Dự án đã có `DashboardStatsScheduler` chạy cron hàng ngày, pattern tương tự sẽ áp dụng cho Leaderboard.
6. **`@CacheEvict` khi data thay đổi:** Đã có pattern evict cache sau khi compute stats.
7. **`AuthUtil.getUserId()`:** Lấy userId từ JWT token trong SecurityContext.

---

## 3. Phân loại Leaderboard

### 3.1. Theo chu kỳ thời gian

| Loại | Mô tả | Reset | Mục đích |
|---|---|---|---|
| `WEEKLY` | Xếp hạng tuần (Thứ 2 → Chủ nhật) | 00:00 UTC mỗi Monday | Tạo cuộc đua ngắn hạn, ai cũng có cơ hội |
| `MONTHLY` | Xếp hạng tháng | 00:00 UTC ngày 1 mỗi tháng | Đánh giá nỗ lực trung hạn |
| `ALL_TIME` | Tổng XP tích lũy toàn bộ | Không reset | Vinh danh học viên trung thành |

### 3.2. Theo phạm vi (Scope)

| Loại | Mô tả |
|---|---|
| `GLOBAL` | Tất cả người dùng trong hệ thống |
| `BY_TARGET_BAND` | Nhóm theo band mục tiêu (5.0, 6.0, 6.5, 7.0+) — cạnh tranh công bằng |
| `FRIENDS` | Giữa những người dùng đã kết bạn _(Phase sau)_ |

---

## 4. Hệ thống XP (Experience Points)

### 4.1. Nguồn XP

| Hoạt động | XP | Giới hạn/ngày |
|---|---|---|
| Hoàn thành Daily Task | +10 XP / task | 4 tasks |
| Hoàn thành tất cả Daily Tasks | +20 XP (bonus) | 1 lần |
| Duy trì Streak | +5 XP × streak_days (cap 50) | 1 lần |
| Hoàn thành Full Test (`IeltsTestAttempt` COMPLETED) | +50 XP | 3 tests |
| Hoàn thành Mini Test | +15 XP | 10 tests |
| Học từ vựng (mỗi 10 `VocabularyRecord`) | +5 XP | 100 từ |
| Quick Practice | +8 XP / session | 10 sessions |
| Speaking Practice (`SpeakingAttempt`) | +12 XP | 5 sessions |
| Writing Submission (`WritingSubmission`) | +15 XP | 3 submissions |
| Band tăng (bất kỳ skill) | +100 XP (one-time per level) | Không giới hạn |

### 4.2. Anti-Gaming Rules

| Quy tắc | Giá trị |
|---|---|
| XP tối đa/ngày | **300 XP** |
| Hoàn thành test dưới 30% thời gian tối thiểu | Không tính XP |
| Cùng 1 test làm lại | XP giảm **50%** từ lần thứ 2 |
| Pattern bất thường (VD: 50 test/giờ) | Flag tài khoản để admin review |

---

## 5. Thiết kế Frontend

### 5.1. Cấu trúc Component

```
LeaderboardPage
├── LeaderboardHeader
│   ├── TimeFilterTabs (Weekly | Monthly | All-Time)
│   └── ScopeFilterDropdown (Global | By Target Band)
├── MyRankCard
│   ├── CurrentRank (#position)
│   ├── UserAvatar + DisplayName
│   ├── TotalXP (trong chu kỳ)
│   ├── XPToNextRank ("23 XP to rank #14")
│   └── RankChangeIndicator (↑3, ↓1, ─)
├── TopThreePodium
│   ├── SecondPlace (left)
│   ├── FirstPlace (center, highlighted)
│   └── ThirdPlace (right)
├── LeaderboardTable
│   ├── LeaderboardRow (repeating)
│   │   ├── RankNumber (with medal icon for top 3)
│   │   ├── UserAvatar
│   │   ├── DisplayName + TargetBand badge
│   │   ├── XPAmount
│   │   ├── StreakBadge (🔥 12)
│   │   └── RankChangeIndicator
│   └── InfiniteScrollLoader
└── WeeklyRewardBanner
    └── RewardTiers (Top 1: Gold, Top 10: Silver, Top 50: Bronze)
```

### 5.2. Widget trên Dashboard Home

Widget nhỏ gọn tích hợp vào `MainGridContainer` của Dashboard (xem `user_home_system_design.md`):

```
┌─────────────────────────────────┐
│  🏆 Weekly Leaderboard          │
│  ─────────────────────────────  │
│  #1  🥇 Minh Anh     1,250 XP  │
│  #2  🥈 Hải Đăng     1,180 XP  │
│  #3  🥉 Thu Hà       1,050 XP  │
│  ─────────────────────────────  │
│  Your rank: #15 / 1,203 users   │
│  📈 ↑3 from yesterday           │
│  [View Full Leaderboard →]      │
└─────────────────────────────────┘
```

---

## 6. Thiết kế Backend

### 6.1. API Endpoints

#### 6.1.1. `GET /api/leaderboard` — Lấy bảng xếp hạng

**Query Parameters:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `period` | enum | `WEEKLY` | `WEEKLY`, `MONTHLY`, `ALL_TIME` |
| `scope` | enum | `GLOBAL` | `GLOBAL`, `BY_TARGET_BAND` |
| `targetBand` | float | null | Bắt buộc nếu scope = `BY_TARGET_BAND` |
| `page` | int | 0 | Trang (0-indexed) |
| `size` | int | 20 | Số lượng/trang (max 50) |

**Response:**

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "myRank": {
      "rank": 15,
      "totalParticipants": 1203,
      "xp": 680,
      "xpToNextRank": 23,
      "rankChange": 3,
      "rankChangeDirection": "UP"
    },
    "topUsers": [
      {
        "rank": 1,
        "userId": "uuid-001",
        "displayName": "Minh Anh",
        "avatarUrl": "/avatars/uuid-001.jpg",
        "targetBand": 7.0,
        "xp": 1250,
        "currentStreak": 30,
        "rankChange": 0,
        "rankChangeDirection": "STABLE"
      }
    ],
    "page": {
      "currentPage": 0,
      "totalPages": 61,
      "totalElements": 1203
    }
  }
}
```

#### 6.1.2. `GET /api/leaderboard/summary` — Widget cho Dashboard Home

**Response:**

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "period": "WEEKLY",
    "myRank": {
      "rank": 15,
      "totalParticipants": 1203,
      "xp": 680,
      "rankChange": 3,
      "rankChangeDirection": "UP"
    },
    "topThree": [
      {
        "rank": 1,
        "displayName": "Minh Anh",
        "avatarUrl": "/avatars/uuid-001.jpg",
        "xp": 1250
      }
    ]
  }
}
```

#### 6.1.3. `GET /api/xp/history` — Lịch sử XP cá nhân

**Query Parameters:** `page` (int, default 0), `size` (int, default 20)

**Response:**

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "totalXP": 2450,
    "weeklyXP": 680,
    "history": [
      {
        "id": "xp-uuid-001",
        "source": "FULL_TEST_COMPLETE",
        "description": "Completed IELTS Academic Test #12",
        "xp": 50,
        "earnedAt": "2025-01-15T08:30:00Z"
      }
    ],
    "page": {
      "currentPage": 0,
      "totalPages": 5,
      "totalElements": 98
    }
  }
}
```

#### 6.1.4. `POST /api/xp/earn` — Ghi nhận XP (Internal)

> API này **không public**, chỉ được gọi nội bộ (service-to-service) khi người dùng hoàn thành hoạt động.

**Request Body:**

```json
{
  "userId": "uuid-user",
  "source": "DAILY_TASK_COMPLETE",
  "sourceId": "task-101",
  "xpAmount": 10
}
```

---

### 6.2. Database Design

#### 6.2.1. ERD

```
┌──────────────────────┐      ┌──────────────────────────┐
│       users           │      │     user_xp_logs         │
│──────────────────────│      │──────────────────────────│
│ id (PK, UUID)        │──┐   │ id (PK, UUID)            │
│ email                │  │   │ user_id (FK → users)     │
│ display_name         │  │   │ source (VARCHAR)         │
│ total_xp   ← NEW    │  ├──<│ source_id (VARCHAR)      │
│ ...                  │  │   │ xp_amount (INT)          │
└──────────────────────┘  │   │ earned_at (TIMESTAMPTZ)  │
                          │   └──────────────────────────┘
                          │
                          │   ┌──────────────────────────────┐
                          │   │  leaderboard_snapshots       │
                          │   │──────────────────────────────│
                          ├──<│ id (UUID, PK)                │
                              │ user_id (FK → users)         │
                              │ period_type (VARCHAR)        │
                              │ period_key (VARCHAR)         │
                              │ scope (VARCHAR)              │
                              │ xp (INT)                     │
                              │ rank (INT)                   │
                              │ previous_rank (INT, nullable)│
                              │ snapshot_date (DATE)         │
                              │ created_at (TIMESTAMPTZ)     │
                              └──────────────────────────────┘

┌──────────────────────────────┐
│  user_daily_xp_cap           │
│──────────────────────────────│
│ id (UUID, PK)                │
│ user_id (FK → users)         │
│ date (DATE)                  │
│ total_xp_earned (INT)        │
│ UNIQUE(user_id, date)        │
└──────────────────────────────┘
```

#### 6.2.2. Migration SQL (`V12__leaderboard.sql`)

> Tuân theo convention hiện tại: UUID PK, `gen_random_uuid()`, PostgreSQL syntax, file đặt trong `docs/`.

```sql
-- =============================================
-- V12: Leaderboard & XP System
-- PostgreSQL
-- =============================================

-- 1. Thêm total_xp vào bảng users
ALTER TABLE users ADD COLUMN IF NOT EXISTS total_xp INT NOT NULL DEFAULT 0;

-- 2. Bảng ghi nhận XP activity
CREATE TABLE user_xp_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source          VARCHAR(50) NOT NULL,
    source_id       VARCHAR(100),
    xp_amount       INT NOT NULL DEFAULT 0,
    earned_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_xp_logs_user_earned ON user_xp_logs(user_id, earned_at DESC);
CREATE INDEX idx_xp_logs_source ON user_xp_logs(source, source_id);

-- 3. Bảng snapshot leaderboard (pre-computed)
CREATE TABLE leaderboard_snapshots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    period_type     VARCHAR(20) NOT NULL,   -- WEEKLY, MONTHLY, ALL_TIME
    period_key      VARCHAR(20) NOT NULL,   -- 2025-W03, 2025-01, ALL
    scope           VARCHAR(30) NOT NULL DEFAULT 'GLOBAL',
    xp              INT NOT NULL DEFAULT 0,
    rank            INT NOT NULL DEFAULT 0,
    previous_rank   INT,
    snapshot_date   DATE NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_snapshot
    ON leaderboard_snapshots(user_id, period_type, period_key, scope, snapshot_date);
CREATE INDEX idx_leaderboard_period_scope
    ON leaderboard_snapshots(period_type, period_key, scope, rank);
CREATE INDEX idx_leaderboard_user
    ON leaderboard_snapshots(user_id, period_type, period_key);

-- 4. Bảng tracking XP cap hàng ngày
CREATE TABLE user_daily_xp_cap (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date            DATE NOT NULL,
    total_xp_earned INT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_user_daily_xp ON user_daily_xp_cap(user_id, date);
```

---

### 6.3. Entity Classes

> Tuân theo pattern dự án: `@Entity`, `@Data`, `@Builder`, UUID PK, Lombok annotations.

#### `UserXpLog.java`

```java
package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_xp_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserXpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "source", length = 50, nullable = false)
    private String source;

    @Column(name = "source_id", length = 100)
    private String sourceId;

    @Column(name = "xp_amount", nullable = false)
    @Builder.Default
    private Integer xpAmount = 0;

    @Column(name = "earned_at", nullable = false)
    @Builder.Default
    private Instant earnedAt = Instant.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
```

#### `LeaderboardSnapshot.java`

```java
package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leaderboard_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "period_type", length = 20, nullable = false)
    private String periodType;

    @Column(name = "period_key", length = 20, nullable = false)
    private String periodKey;

    @Column(name = "scope", length = 30, nullable = false)
    @Builder.Default
    private String scope = "GLOBAL";

    @Column(name = "xp", nullable = false)
    @Builder.Default
    private Integer xp = 0;

    @Column(name = "rank", nullable = false)
    @Builder.Default
    private Integer rank = 0;

    @Column(name = "previous_rank")
    private Integer previousRank;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
```

#### `UserDailyXpCap.java`

```java
package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_daily_xp_cap")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDailyXpCap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_xp_earned", nullable = false)
    @Builder.Default
    private Integer totalXpEarned = 0;
}
```

#### Bổ sung field cho `User.java`

```java
// Thêm vào entity User hiện tại:
@Column(name = "total_xp", nullable = false)
@Builder.Default
private Integer totalXp = 0;
```

---

### 6.4. Enums & Constants

```java
// com.swpts.enpracticebe.constant.LeaderboardPeriod
public enum LeaderboardPeriod {
    WEEKLY, MONTHLY, ALL_TIME
}

// com.swpts.enpracticebe.constant.LeaderboardScope
public enum LeaderboardScope {
    GLOBAL, BY_TARGET_BAND
}

// com.swpts.enpracticebe.constant.RankChangeDirection
public enum RankChangeDirection {
    UP, DOWN, STABLE, NEW
}

// com.swpts.enpracticebe.constant.XpSource
public enum XpSource {
    DAILY_TASK_COMPLETE,
    ALL_DAILY_TASKS_BONUS,
    STREAK_BONUS,
    FULL_TEST_COMPLETE,
    MINI_TEST_COMPLETE,
    VOCABULARY_REVIEW,
    QUICK_PRACTICE,
    SPEAKING_PRACTICE,
    WRITING_SUBMISSION,
    BAND_INCREASE
}

// Thêm vào com.swpts.enpracticebe.constant.Constants
public static final int MAX_DAILY_XP = 300;
public static final int REPEAT_XP_PENALTY_PERCENT = 50;
```

---

### 6.5. DTO Response Classes

> Tuân theo pattern: `@Data`, `@Builder`, package `dto.response.leaderboard`.

#### `LeaderboardResponse.java`

```java
@Data
@Builder
public class LeaderboardResponse {
    private MyRankInfo myRank;
    private List<LeaderboardEntry> topUsers;
    private PageResponse page;
}
```

#### `MyRankInfo.java`

```java
@Data
@Builder
public class MyRankInfo {
    private int rank;
    private int totalParticipants;
    private int xp;
    private int xpToNextRank;
    private int rankChange;
    private RankChangeDirection rankChangeDirection;
}
```

#### `LeaderboardEntry.java`

```java
@Data
@Builder
public class LeaderboardEntry {
    private int rank;
    private UUID userId;
    private String displayName;
    private String avatarUrl;
    private Float targetBand;
    private int xp;
    private int currentStreak;
    private int rankChange;
    private RankChangeDirection rankChangeDirection;
}
```

#### `LeaderboardSummaryResponse.java`

```java
@Data
@Builder
public class LeaderboardSummaryResponse {
    private LeaderboardPeriod period;
    private MyRankInfo myRank;
    private List<LeaderboardEntry> topThree;
}
```

#### `XpHistoryResponse.java`

```java
@Data
@Builder
public class XpHistoryResponse {
    private int totalXP;
    private int weeklyXP;
    private List<XpHistoryEntry> history;
    private PageResponse page;
}
```

#### `XpHistoryEntry.java`

```java
@Data
@Builder
public class XpHistoryEntry {
    private UUID id;
    private XpSource source;
    private String description;
    private int xp;
    private Instant earnedAt;
}
```

---

### 6.6. Service Layer Architecture

```
Controller Layer
├── LeaderboardController                      (REST: /api/leaderboard)
│   ├── getLeaderboard(period, scope, targetBand, page, size)
│   └── getLeaderboardSummary()
├── XpController                               (REST: /api/xp)
│   └── getXpHistory(page, size)

Service Layer
├── LeaderboardService (interface)
│   ├── getLeaderboard(...)
│   ├── getLeaderboardSummary(...)
│   └── computeAndSnapshotRanks(period)
├── LeaderboardServiceImpl (implementation)
│   ├── @Cacheable getLeaderboard(...)         → Query snapshot table
│   ├── @Cacheable getLeaderboardSummary(...)  → Top 3 + my rank
│   └── computeAndSnapshotRanks(period)        → Aggregate XP → Upsert snapshots
├── XpService (interface)
│   ├── earnXp(userId, source, sourceId, amount)
│   └── getXpHistory(userId, page, size)
├── XpServiceImpl (implementation)
│   ├── earnXp(...)                            → Validate cap → Insert log → Update user.totalXp → Evict cache
│   └── getXpHistory(...)                      → Query xp_logs
├── LeaderboardScheduler                       → @Scheduled cron jobs
│   ├── computeWeeklyRanks()                   → Mỗi 15 phút
│   ├── computeMonthlyRanks()                  → Mỗi 1 giờ
│   ├── computeAllTimeRanks()                  → Mỗi 6 giờ
│   └── archiveExpiredSnapshots()              → Mỗi ngày 02:00 UTC

Repository Layer
├── UserXpLogRepository
├── LeaderboardSnapshotRepository
└── UserDailyXpCapRepository
```

---

### 6.7. Caching Strategy (Caffeine)

> Tích hợp vào `CacheConfig.java` hiện tại theo đúng pattern 2-tier đã có.

#### Đăng ký cache names mới

```java
// Thêm vào LONG_TTL_CACHES (60 min):
"xpHistory"

// Thêm vào SHORT_TTL_CACHES (5 min):
"leaderboardPage", "leaderboardSummary", "userRank"
```

#### Chi tiết cache

| Cache Name | Key Pattern | TTL | Max Size | Mô tả |
|---|---|---|---|---|
| `leaderboardPage` | `{period}:{scope}:{targetBand}:{page}:{size}` | 5 min | 200 | Trang leaderboard đầy đủ |
| `leaderboardSummary` | `{userId}:{period}` | 5 min | 500 | Widget summary cho Dashboard |
| `userRank` | `{userId}:{period}:{scope}` | 5 min | 1000 | Rank cá nhân |
| `xpHistory` | `{userId}:{page}:{size}` | 60 min | 300 | Lịch sử XP cá nhân |

#### Eviction Rules

| Sự kiện | Cache bị evict |
|---|---|
| `XpService.earnXp()` được gọi | `leaderboardSummary`, `userRank`, `xpHistory` của user đó |
| `LeaderboardScheduler.computeRanks()` chạy | Toàn bộ `leaderboardPage` cho period tương ứng |

#### Ví dụ code

```java
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    @Cacheable(value = "leaderboardPage",
               key = "#period + ':' + #scope + ':' + #targetBand + ':' + #page + ':' + #size")
    @Override
    public LeaderboardResponse getLeaderboard(LeaderboardPeriod period,
                                               LeaderboardScope scope,
                                               Float targetBand,
                                               int page, int size) {
        // Query from leaderboard_snapshots + users JOIN
    }

    @Cacheable(value = "leaderboardSummary",
               key = "#userId + ':' + #period")
    @Override
    public LeaderboardSummaryResponse getLeaderboardSummary(UUID userId,
                                                             LeaderboardPeriod period) {
        // Query top 3 + user's own rank
    }

    @CacheEvict(value = "leaderboardPage", allEntries = true)
    @Override
    public void computeAndSnapshotRanks(LeaderboardPeriod period) {
        // Aggregate XP → compute RANK() → upsert leaderboard_snapshots
    }
}
```

```java
@Service
@RequiredArgsConstructor
public class XpServiceImpl implements XpService {

    @Caching(evict = {
        @CacheEvict(value = "leaderboardSummary", key = "#userId + ':WEEKLY'"),
        @CacheEvict(value = "leaderboardSummary", key = "#userId + ':MONTHLY'"),
        @CacheEvict(value = "leaderboardSummary", key = "#userId + ':ALL_TIME'"),
        @CacheEvict(value = "userRank", allEntries = true),
        @CacheEvict(value = "xpHistory", key = "#userId + ':0:20'")
    })
    @Override
    public void earnXp(UUID userId, XpSource source, String sourceId, int amount) {
        // 1. Check daily cap
        // 2. Check anti-gaming rules
        // 3. Insert user_xp_logs
        // 4. Update users.total_xp
        // 5. Upsert user_daily_xp_cap
    }
}
```

---

### 6.8. Scheduled Jobs Chi tiết

> Tuân theo pattern `DashboardStatsScheduler` hiện tại: `@Scheduled` + `@Transactional` + `@CacheEvict`.

#### `LeaderboardScheduler.java`

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardScheduler {

    private final LeaderboardService leaderboardService;

    // Mỗi 15 phút — Weekly leaderboard cần cập nhật thường xuyên
    @Scheduled(cron = "0 */15 * * * *")
    public void computeWeeklyRanks() {
        log.info("Computing weekly leaderboard ranks...");
        leaderboardService.computeAndSnapshotRanks(LeaderboardPeriod.WEEKLY);
        log.info("Weekly leaderboard computation completed.");
    }

    // Mỗi 1 giờ — Monthly leaderboard ít thay đổi hơn
    @Scheduled(cron = "0 0 * * * *")
    public void computeMonthlyRanks() {
        log.info("Computing monthly leaderboard ranks...");
        leaderboardService.computeAndSnapshotRanks(LeaderboardPeriod.MONTHLY);
        log.info("Monthly leaderboard computation completed.");
    }

    // Mỗi 6 giờ — All-time chỉ cần cập nhật vài lần/ngày
    @Scheduled(cron = "0 0 */6 * * *")
    public void computeAllTimeRanks() {
        log.info("Computing all-time leaderboard ranks...");
        leaderboardService.computeAndSnapshotRanks(LeaderboardPeriod.ALL_TIME);
        log.info("All-time leaderboard computation completed.");
    }

    // Mỗi ngày 02:00 UTC — Dọn dẹp snapshot cũ
    @Scheduled(cron = "0 0 2 * * *")
    public void archiveExpiredSnapshots() {
        log.info("Archiving expired leaderboard snapshots...");
        // Giữ snapshot mới nhất mỗi period đã kết thúc > 2 tuần
        // Xóa snapshots trung gian
        log.info("Archive completed.");
    }
}
```

#### SQL Logic cho Rank Computation

```sql
-- Step 1: Aggregate XP cho tuần hiện tại
WITH weekly_xp AS (
    SELECT user_id, SUM(xp_amount) AS total_xp
    FROM user_xp_logs
    WHERE earned_at >= :weekStart AND earned_at < :weekEnd
    GROUP BY user_id
),
ranked AS (
    SELECT
        user_id,
        total_xp,
        RANK() OVER (ORDER BY total_xp DESC) AS new_rank
    FROM weekly_xp
)
-- Step 2: Upsert vào leaderboard_snapshots
INSERT INTO leaderboard_snapshots (id, user_id, period_type, period_key, scope, xp, rank, previous_rank, snapshot_date)
SELECT
    gen_random_uuid(),
    r.user_id,
    'WEEKLY',
    :periodKey,        -- VD: '2025-W03'
    'GLOBAL',
    r.total_xp,
    r.new_rank,
    ls.rank,           -- previous_rank từ snapshot trước
    CURRENT_DATE
FROM ranked r
LEFT JOIN leaderboard_snapshots ls
    ON ls.user_id = r.user_id
    AND ls.period_type = 'WEEKLY'
    AND ls.period_key = :periodKey
    AND ls.scope = 'GLOBAL'
    AND ls.snapshot_date = (
        SELECT MAX(snapshot_date)
        FROM leaderboard_snapshots
        WHERE user_id = r.user_id
          AND period_type = 'WEEKLY'
          AND period_key = :periodKey
          AND scope = 'GLOBAL'
    )
ON CONFLICT (user_id, period_type, period_key, scope, snapshot_date)
DO UPDATE SET
    xp = EXCLUDED.xp,
    previous_rank = leaderboard_snapshots.rank,
    rank = EXCLUDED.rank;
```

---

### 6.9. XP Earning Flow (Sequence Diagram)

```
User hoàn thành hoạt động (VD: Submit IELTS Test)
         │
         ▼
IeltsTestServiceImpl.submitTest()
         │
         ├──► XpService.earnXp(userId, FULL_TEST_COMPLETE, testId, 50)
         │       │
         │       ├── 1. Query user_daily_xp_cap → nếu >= 300 → return (cap reached)
         │       ├── 2. Check anti-gaming:
         │       │       - Duplicate sourceId? → reject
         │       │       - Test hoàn thành < 30% min time? → reject
         │       │       - Cùng test lần 2+? → xp = xp * 50%
         │       ├── 3. INSERT INTO user_xp_logs
         │       ├── 4. UPDATE users SET total_xp = total_xp + :actualXp
         │       ├── 5. UPSERT user_daily_xp_cap SET total_xp_earned += :actualXp
         │       └── 6. Evict cache: leaderboardSummary, userRank, xpHistory
         │
         └──► Return response (kèm xp earned notification)
```

### 6.10. Controller Implementation

```java
@RestController
@AllArgsConstructor
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final AuthUtil authUtil;

    @GetMapping
    public DefaultResponse<LeaderboardResponse> getLeaderboard(
            @RequestParam(defaultValue = "WEEKLY") LeaderboardPeriod period,
            @RequestParam(defaultValue = "GLOBAL") LeaderboardScope scope,
            @RequestParam(required = false) Float targetBand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return DefaultResponse.success(
            leaderboardService.getLeaderboard(period, scope, targetBand, page, size));
    }

    @GetMapping("/summary")
    public DefaultResponse<LeaderboardSummaryResponse> getSummary() {
        UUID userId = authUtil.getUserId();
        return DefaultResponse.success(
            leaderboardService.getLeaderboardSummary(userId, LeaderboardPeriod.WEEKLY));
    }
}
```

```java
@RestController
@AllArgsConstructor
@RequestMapping("/api/xp")
public class XpController {

    private final XpService xpService;
    private final AuthUtil authUtil;

    @GetMapping("/history")
    public DefaultResponse<XpHistoryResponse> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = authUtil.getUserId();
        return DefaultResponse.success(xpService.getXpHistory(userId, page, size));
    }
}
```

---

## 7. Hiệu năng & Scalability

### 7.1. Chiến lược tối ưu

| Vấn đề | Giải pháp |
|---|---|
| Query XP aggregate nặng khi user nhiều | Pre-compute bằng Scheduled Job → lưu `leaderboard_snapshots` |
| Hot endpoint (nhiều user gọi cùng lúc) | Caffeine Cache TTL 5 min (tuân theo `SHORT_TTL_CACHES` pattern) |
| Bảng `user_xp_logs` phình to | Archive log > 6 tháng vào bảng cold storage |
| Rank computation O(N log N) | Chỉ chạy trong scheduled job, **không** chạy on-request |
| Concurrent XP earning | Optimistic locking trên `user_daily_xp_cap` |
| Dashboard load time | Widget summary trả top 3 + my rank chỉ 1 query nhẹ trên snapshot |

### 7.2. Index Strategy (đã bao gồm trong migration)

| Index | Mục đích |
|---|---|
| `idx_xp_logs_user_earned(user_id, earned_at DESC)` | Tối ưu XP aggregation + history |
| `idx_xp_logs_source(source, source_id)` | Phát hiện duplicate / anti-gaming |
| `uk_snapshot(user_id, period_type, period_key, scope, snapshot_date)` | Upsert ON CONFLICT |
| `idx_leaderboard_period_scope(period_type, period_key, scope, rank)` | Query leaderboard page |
| `idx_leaderboard_user(user_id, period_type, period_key)` | Lookup rank cá nhân |

---

## 8. Phần thưởng cuối tuần (Weekly Rewards)

| Hạng | Phần thưởng |
|---|---|
| **Top 1** | 🥇 Gold Badge + 200 Bonus XP + Profile Crown (hiển thị 1 tuần) |
| **Top 2-3** | 🥈🥉 Silver/Bronze Badge + 100 Bonus XP |
| **Top 4-10** | Achievement Badge + 50 Bonus XP |
| **Top 11-50** | 25 Bonus XP |
| **Tham gia (XP > 0)** | 10 Bonus XP |

Phần thưởng được phát tự động bởi `WeeklyRewardJob` chạy lúc **00:05 UTC Monday** (sau khi `computeWeeklyRanks` chạy lần cuối tuần cũ).

---

## 9. Tích hợp với hệ thống hiện tại

### 9.1. Điểm tích hợp XP earning

| Service hiện tại | Trigger | XP |
|---|---|---|
| `IeltsTestServiceImpl` | Khi `submitTest()` → status = COMPLETED | +50 XP |
| `SpeakingServiceImpl` | Khi submit speaking attempt | +12 XP |
| `WritingServiceImpl` | Khi submit writing | +15 XP |
| `RecordServiceImpl` | Mỗi 10 từ vocabulary record mới | +5 XP |
| `UserDashboardServiceImpl` | Khi hoàn thành daily task | +10 XP |
| `UserDashboardServiceImpl` | Khi hoàn thành tất cả daily tasks | +20 XP bonus |

### 9.2. Tích hợp Dashboard Widget

Thêm field `leaderboardSummary` vào `DashboardResponse`:

```java
// Trong DashboardResponse.java
private LeaderboardSummaryResponse leaderboardSummary;
```

Trong `UserDashboardServiceImpl.getDashboard()`:

```java
CompletableFuture<LeaderboardSummaryResponse> leaderboardFuture =
    CompletableFuture.supplyAsync(() ->
        leaderboardService.getLeaderboardSummary(userId, LeaderboardPeriod.WEEKLY));
```

---

## 10. Lộ trình triển khai

| Phase | Nội dung | Thời gian ước tính |
|---|---|---|
| **3.1** | DB migration (`V12`), Entity classes, Repository interfaces | 2 ngày |
| **3.2** | `XpService` + daily cap + anti-gaming logic | 3 ngày |
| **3.3** | `LeaderboardService` + `LeaderboardScheduler` + Caffeine Cache | 3 ngày |
| **3.4** | REST Controllers + tích hợp XP earning vào services hiện tại | 2 ngày |
| **3.5** | Dashboard widget integration + `LeaderboardSummaryResponse` | 1 ngày |
| **3.6** | Weekly Rewards + anti-gaming monitoring | 2 ngày |
| **3.7** | By Target Band scope + unit/integration tests | 2 ngày |

**Tổng ước tính: ~15 ngày làm việc**

---

## 11. Tóm tắt Files cần tạo/sửa

### Files mới

| File | Package/Location |
|---|---|
| `docs/V12__leaderboard.sql` | Migration SQL |
| `entity/UserXpLog.java` | JPA Entity |
| `entity/LeaderboardSnapshot.java` | JPA Entity |
| `entity/UserDailyXpCap.java` | JPA Entity |
| `constant/LeaderboardPeriod.java` | Enum |
| `constant/LeaderboardScope.java` | Enum |
| `constant/RankChangeDirection.java` | Enum |
| `constant/XpSource.java` | Enum |
| `repository/UserXpLogRepository.java` | Repository |
| `repository/LeaderboardSnapshotRepository.java` | Repository |
| `repository/UserDailyXpCapRepository.java` | Repository |
| `dto/response/leaderboard/LeaderboardResponse.java` | DTO |
| `dto/response/leaderboard/MyRankInfo.java` | DTO |
| `dto/response/leaderboard/LeaderboardEntry.java` | DTO |
| `dto/response/leaderboard/LeaderboardSummaryResponse.java` | DTO |
| `dto/response/leaderboard/XpHistoryResponse.java` | DTO |
| `dto/response/leaderboard/XpHistoryEntry.java` | DTO |
| `service/LeaderboardService.java` | Service interface |
| `service/XpService.java` | Service interface |
| `service/impl/LeaderboardServiceImpl.java` | Service impl |
| `service/impl/XpServiceImpl.java` | Service impl |
| `service/LeaderboardScheduler.java` | Scheduled Jobs |
| `controller/LeaderboardController.java` | REST Controller |
| `controller/XpController.java` | REST Controller |

### Files cần sửa

| File | Thay đổi |
|---|---|
| `entity/User.java` | Thêm field `totalXp` |
| `config/CacheConfig.java` | Thêm cache names: `leaderboardPage`, `leaderboardSummary`, `userRank`, `xpHistory` |
| `constant/Constants.java` | Thêm `MAX_DAILY_XP = 300`, `REPEAT_XP_PENALTY_PERCENT = 50` |
| `dto/response/dashboard/DashboardResponse.java` | Thêm `leaderboardSummary` field |
| `service/impl/UserDashboardServiceImpl.java` | Tích hợp leaderboard summary vào dashboard |
| `service/impl/IeltsTestServiceImpl.java` | Gọi `xpService.earnXp()` khi submit test |
| `service/impl/SpeakingServiceImpl.java` | Gọi `xpService.earnXp()` khi submit speaking |
| `service/impl/WritingServiceImpl.java` | Gọi `xpService.earnXp()` khi submit writing |
| `service/impl/RecordServiceImpl.java` | Gọi `xpService.earnXp()` mỗi 10 từ vựng mới |

