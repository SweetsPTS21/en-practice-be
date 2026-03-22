# Tài liệu handoff FE - User Profile

Tài liệu này dùng cho frontend implement chức năng **User Profile** dựa trên:

* định hướng sản phẩm trong `docs/user-profile.md`
* backend hiện tại đã implement xong API profile summary, profile detail và update profile

Mục tiêu là để FE có thể vào làm ngay mà không cần đọc toàn bộ code backend.

---

## 1. Phạm vi đã có ở backend

Backend hiện đã support 3 API chính:

* `GET /api/user/profile/summary`
* `GET /api/user/profile`
* `PUT /api/user/profile`

Response đều dùng wrapper chung:

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

Lưu ý:

* dropdown ở header chỉ nên gọi `GET /api/user/profile/summary`
* trang user profile chi tiết mới gọi `GET /api/user/profile`
* form edit profile dùng `PUT /api/user/profile`

---

## 2. Mục tiêu UI theo từng use case

### 2.1. Header avatar dropdown

Mục tiêu:

* hiển thị nhanh thông tin cá nhân và động lực học
* tải nhẹ
* mở nhanh khi user click avatar

Thông tin nên hiển thị:

* avatar
* display name
* email
* current level
* total XP hoặc progress tới level tiếp theo
* target IELTS band
* overall band hiện tại nếu có
* current streak
* weekly XP
* CTA:
  * `Xem chi tiết profile`
  * `Chỉnh sửa profile`

### 2.2. Trang User Profile chi tiết

Mục tiêu:

* là dashboard học tập cá nhân
* tập trung vào tiến độ, goals, streak, vocabulary, recommendations
* cho phép edit thông tin và mục tiêu học tập

Section nên có:

* Profile header
* Overview cards
* Level / XP progress
* Goals
* Today goal
* Skill progress 4 kỹ năng
* Vocabulary overview
* Streak + 30-day heatmap
* Weak skills
* Recommended practice
* Recent activity
* Leaderboard summary

---

## 3. Chiến lược fetch dữ liệu FE

Khuyến nghị dùng 2 query tách biệt:

### 3.1. Query cho dropdown

Query key gợi ý:

* `['user-profile-summary']`

Gọi khi:

* app load xong user session
* hoặc khi mở dropdown lần đầu

Dùng cho:

* avatar menu
* mini profile card ở header

### 3.2. Query cho profile detail

Query key gợi ý:

* `['user-profile-detail']`

Chỉ gọi khi:

* user vào trang `/profile` hoặc route tương đương

Không nên:

* gọi full profile ngay từ header
* prefetch full profile quá sớm nếu chưa có nhu cầu

### 3.3. Mutation update profile

Query invalidation gợi ý sau khi update thành công:

* `['user-profile-summary']`
* `['user-profile-detail']`
* query user session/me nếu app đang hiển thị avatar, display name ở chỗ khác

---

## 4. API contract cho FE

## 4.1. GET `/api/user/profile/summary`

Mục đích:

* dùng cho header dropdown

### Response shape

```ts
type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

type UserProfileSummaryResponse = {
  id: string;
  email: string;
  displayName: string;
  avatarUrl: string | null;
  goals: {
    targetIeltsBand: number | null;
    targetExamDate: string | null;
    dailyGoalMinutes: number | null;
    weeklyWordGoal: number | null;
    preferredSkill: string | null;
  };
  levelInfo: {
    totalXp: number;
    currentLevel: number;
    currentLevelMinXp: number;
    nextLevel: number;
    nextLevelMinXp: number;
    xpIntoCurrentLevel: number;
    xpNeededForNextLevel: number;
    progressPercentage: number;
  };
  overallBand: number | null;
  currentStreak: number | null;
  weeklyXp: number | null;
};
```

### Gợi ý render dropdown

* Dòng 1: avatar + displayName
* Dòng 2: email
* Badge nhỏ:
  * `Level {currentLevel}`
  * `Target {targetIeltsBand}` nếu có
  * `{currentStreak} day streak` nếu > 0
* Progress bar:
  * dùng `levelInfo.progressPercentage`
  * caption: `{xpIntoCurrentLevel} XP trong level hiện tại`
* Thông tin phụ:
  * `Overall band {overallBand}` nếu có
  * `Weekly XP {weeklyXp}`

### Empty/fallback gợi ý

* `avatarUrl` null: dùng initials từ `displayName`
* `targetIeltsBand` null: hiển thị `Chưa đặt mục tiêu`
* `overallBand` null: hiển thị `Chưa có band estimate`
* `currentStreak` null hoặc `0`: hiển thị `Bắt đầu streak hôm nay`

---

## 4.2. GET `/api/user/profile`

Mục đích:

* dùng cho trang profile chi tiết

### Response shape

```ts
type UserProfileResponse = {
  id: string;
  email: string;
  displayName: string;
  avatarUrl: string | null;
  bio: string | null;
  createdAt: string;
  lastLoginAt: string | null;
  goals: {
    targetIeltsBand: number | null;
    targetExamDate: string | null;
    dailyGoalMinutes: number | null;
    weeklyWordGoal: number | null;
    preferredSkill: string | null;
  };
  levelInfo: {
    totalXp: number;
    currentLevel: number;
    currentLevelMinXp: number;
    nextLevel: number;
    nextLevelMinXp: number;
    xpIntoCurrentLevel: number;
    xpNeededForNextLevel: number;
    progressPercentage: number;
  };
  overview: {
    weeklyXp: number;
    totalLessonsCompleted: number;
    totalWordsLearned: number;
    totalStudyMinutes: number;
    wordsToReviewToday: number;
    currentStreak: number;
    longestStreak: number;
  };
  streak: {
    currentStreak: number;
    longestStreak: number;
    activeDaysLast30: number;
    heatmap: Array<{
      date: string;
      hasActivity: boolean;
    }>;
  };
  todayGoal: {
    targetMinutes: number;
    studiedMinutes: number;
    percentage: number;
  };
  bandProgress: {
    listening: { current: number | null; previous: number | null };
    reading: { current: number | null; previous: number | null };
    writing: { current: number | null; previous: number | null };
    speaking: { current: number | null; previous: number | null };
    overall: { current: number | null; previous: number | null };
  };
  vocabProgress: {
    totalWords: number;
    masteredWords: number;
    reviewingWords: number;
  };
  dictionaryStats: {
    totalWords: number;
    favoriteWords: number;
    wordsToReviewToday: number;
    newWords: number;
    learningWords: number;
    masteredWords: number;
  };
  weakSkills: string[];
  recommendedPractice: Array<{
    id: string;
    title: string;
    description: string;
    type: string;
    difficulty: string;
    estimatedTime: string;
    path: string;
    reason: string;
    priority: number | null;
  }>;
  recentActivities: Array<{
    id: string;
    title: string;
    type: string;
    score: string | null;
    description: string | null;
    timestamp: string;
  }>;
  leaderboardSummary: {
    period: 'WEEKLY' | string;
    myRank: {
      rank: number;
      totalParticipants: number;
      xp: number;
      xpToNextRank: number;
      rankChange: number;
      rankChangeDirection: 'UP' | 'DOWN' | 'NONE' | string;
    } | null;
    topThree: Array<{
      rank: number;
      userId: string;
      displayName: string;
      avatarUrl: string | null;
      targetBand: number | null;
      xp: number;
      currentStreak: number;
      rankChange: number;
      rankChangeDirection: 'UP' | 'DOWN' | 'NONE' | string;
    }>;
  } | null;
};
```

### Gợi ý mapping section

#### A. ProfileHeader

Dùng:

* `displayName`
* `email`
* `avatarUrl`
* `bio`
* `createdAt`
* `goals.targetIeltsBand`
* `levelInfo.currentLevel`
* `overview.currentStreak`

CTA:

* `Edit profile`

#### B. Level / XP card

Dùng:

* `levelInfo.totalXp`
* `levelInfo.currentLevel`
* `levelInfo.nextLevel`
* `levelInfo.progressPercentage`
* `levelInfo.xpNeededForNextLevel`

UI:

* thanh progress bar
* label `Còn {xpNeededForNextLevel} XP để lên level {nextLevel}`

#### C. Overview cards

Dùng:

* `overview.weeklyXp`
* `overview.totalLessonsCompleted`
* `overview.totalWordsLearned`
* `overview.totalStudyMinutes`
* `overview.wordsToReviewToday`
* `overview.currentStreak`

Gợi ý format:

* `totalStudyMinutes` nên convert ra giờ + phút để dễ đọc

#### D. Goals card

Dùng:

* `goals.targetIeltsBand`
* `goals.targetExamDate`
* `goals.dailyGoalMinutes`
* `goals.weeklyWordGoal`
* `goals.preferredSkill`

#### E. TodayGoal card

Dùng:

* `todayGoal.targetMinutes`
* `todayGoal.studiedMinutes`
* `todayGoal.percentage`

UI:

* progress ring hoặc progress bar
* message:
  * nếu `percentage >= 100`: `Bạn đã hoàn thành mục tiêu hôm nay`
  * nếu `< 100`: `Còn {targetMinutes - studiedMinutes} phút để đạt goal`

#### F. SkillProgressSection

Dùng:

* `bandProgress.listening.current`
* `bandProgress.reading.current`
* `bandProgress.writing.current`
* `bandProgress.speaking.current`
* `bandProgress.overall.current`

Lưu ý:

* `previous` hiện backend đang trả `0` mặc định, chưa phải delta thật
* FE nên ưu tiên hiển thị `current`
* nếu muốn hiển thị trend, nên chỉ hiện khi `previous` có ý nghĩa thực tế

#### G. VocabularyOverview

Dùng:

* `vocabProgress.totalWords`
* `vocabProgress.masteredWords`
* `vocabProgress.reviewingWords`
* `dictionaryStats.favoriteWords`
* `dictionaryStats.newWords`
* `dictionaryStats.learningWords`
* `dictionaryStats.wordsToReviewToday`

Gợi ý chart:

* donut cho `mastered / learning / review`
* stat cards cho `favorite`, `new`, `review today`

#### H. StreakCalendar

Dùng:

* `streak.currentStreak`
* `streak.longestStreak`
* `streak.activeDaysLast30`
* `streak.heatmap`

Format heatmap:

```ts
type DayStatus = {
  date: string; // yyyy-MM-dd
  hasActivity: boolean;
};
```

Gợi ý render:

* grid 30 ô hoặc calendar heatmap
* `hasActivity = true` tô màu nổi
* tooltip: ngày + trạng thái có học hay không

#### I. WeakSkills

Dùng:

* `weakSkills`

UI:

* tag list hoặc alert box
* message kiểu:
  * `Bạn nên ưu tiên cải thiện: ...`

#### J. RecommendationPanel

Dùng:

* `recommendedPractice`

Mỗi item nên hiển thị:

* title
* description
* type
* difficulty
* estimatedTime
* reason
* CTA `Luyện ngay`

Khi click:

* navigate tới `path`

#### K. RecentActivityTimeline

Dùng:

* `recentActivities`

Mỗi item:

* title
* type
* score
* description
* timestamp

#### L. LeaderboardSummary

Dùng:

* `leaderboardSummary.myRank`
* `leaderboardSummary.topThree`

UI tối giản:

* card `Xếp hạng tuần`
* hiển thị rank hiện tại, tổng số người tham gia, XP còn thiếu để lên hạng tiếp theo
* top 3 learners

---

## 4.3. PUT `/api/user/profile`

Mục đích:

* cập nhật thông tin cá nhân và goals

### Request body

```ts
type UpdateUserProfileRequest = {
  displayName?: string;
  avatarUrl?: string;
  bio?: string;
  targetIeltsBand?: number;
  targetExamDate?: string; // yyyy-MM-dd
  dailyGoalMinutes?: number;
  weeklyWordGoal?: number;
  preferredSkill?: string;
};
```

### Validation cần match với backend

* `displayName`: 1-100 ký tự
* `avatarUrl`: tối đa 500 ký tự
* `bio`: tối đa 500 ký tự
* `targetIeltsBand`: từ `0.0` đến `9.0`
* `dailyGoalMinutes`: từ `1` đến `1440`
* `weeklyWordGoal`: từ `1` đến `10000`
* `preferredSkill`: tối đa 50 ký tự

### Hành vi lưu ý

* backend cho phép partial update
* field nào không gửi lên thì giữ nguyên
* chuỗi rỗng có thể bị normalize thành `null` ở BE cho một số field text

### Sau khi update thành công

FE nên:

* đóng modal hoặc form edit
* show toast thành công
* invalidate query summary và detail
* cập nhật avatar/name ở header nếu app đang cache riêng

---

## 5. Gợi ý cấu trúc component FE

Có thể tách theo hướng sau:

```txt
features/profile/
  api/
    getUserProfileSummary.ts
    getUserProfileDetail.ts
    updateUserProfile.ts
  hooks/
    useUserProfileSummary.ts
    useUserProfileDetail.ts
    useUpdateUserProfile.ts
  types/
    user-profile.ts
  components/
    ProfileDropdownCard.tsx
    ProfileHeader.tsx
    ProfileLevelCard.tsx
    ProfileOverviewCards.tsx
    ProfileGoalsCard.tsx
    ProfileTodayGoalCard.tsx
    ProfileSkillSection.tsx
    ProfileVocabularySection.tsx
    ProfileStreakHeatmap.tsx
    ProfileWeakSkills.tsx
    ProfileRecommendations.tsx
    ProfileRecentActivities.tsx
    ProfileLeaderboardCard.tsx
    EditProfileModal.tsx
  pages/
    UserProfilePage.tsx
```

---

## 6. Gợi ý user flow

## 6.1. Header dropdown flow

1. User login xong.
2. FE fetch `GET /api/user/profile/summary`.
3. Render mini profile card trong avatar dropdown.
4. Click `Xem chi tiết` thì chuyển sang trang profile.

## 6.2. Profile page flow

1. User vào route profile.
2. FE fetch `GET /api/user/profile`.
3. Render toàn bộ section.
4. User click edit.
5. Submit `PUT /api/user/profile`.
6. Invalidate summary + detail.

---

## 7. Loading, empty state, error state

### Loading

Header dropdown:

* dùng skeleton nhỏ cho avatar, name, 2-3 info rows

Profile page:

* skeleton từng section
* không nên chặn cả page bằng spinner toàn màn hình nếu đã có layout

### Empty state

Các trường có thể chưa có dữ liệu:

* `bio`
* `targetIeltsBand`
* `targetExamDate`
* `preferredSkill`
* `overallBand`
* `recentActivities`
* `recommendedPractice`

Gợi ý:

* dùng copy tích cực như `Thêm mục tiêu IELTS để cá nhân hóa lộ trình`
* không hiển thị card rỗng gây cảm giác lỗi

### Error state

* dropdown lỗi: fallback về menu đơn giản với avatar/name
* detail page lỗi: có retry button
* update lỗi: hiển thị message từ API nếu có

---

## 8. Những phần chưa có hoặc chưa nên làm ở FE

Hiện tại backend profile chưa implement riêng:

* achievements / badges
* social / follow
* profile public của user khác
* upload avatar riêng dạng file API

Do đó FE ở giai đoạn này:

* không cần dựng tab achievements thật
* không cần nút share/follow
* avatar hiện tại là nhập `avatarUrl`, không phải upload file

---

## 9. Gợi ý format hiển thị

### Date

* `createdAt`, `lastLoginAt`, `timestamp` đang trả theo format `yyyy-MM-dd HH:mm:ss`
* `targetExamDate` và `heatmap.date` là `yyyy-MM-dd`

### Number

* Band IELTS: format 1 chữ số thập phân nếu cần
* XP: có thể dùng `1,250 XP`
* Streak: `7 days`
* Study time: convert phút sang `xh ym`

---

## 10. Ưu tiên implement đề xuất cho FE

### Phase 1

* gọi và render `GET /api/user/profile/summary`
* làm dropdown card ở header
* route sang profile detail page

### Phase 2

* dựng page detail với section:
  * header
  * level/xp
  * overview cards
  * goals
  * today goal
  * skill progress
  * vocabulary
  * streak heatmap

### Phase 3

* recommendation panel
* recent activities
* leaderboard summary
* edit profile modal

---

## 11. Ví dụ type FE đầy đủ

```ts
export type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

export type LevelInfo = {
  totalXp: number;
  currentLevel: number;
  currentLevelMinXp: number;
  nextLevel: number;
  nextLevelMinXp: number;
  xpIntoCurrentLevel: number;
  xpNeededForNextLevel: number;
  progressPercentage: number;
};

export type UserGoals = {
  targetIeltsBand: number | null;
  targetExamDate: string | null;
  dailyGoalMinutes: number | null;
  weeklyWordGoal: number | null;
  preferredSkill: string | null;
};

export type UserProfileSummary = {
  id: string;
  email: string;
  displayName: string;
  avatarUrl: string | null;
  goals: UserGoals;
  levelInfo: LevelInfo;
  overallBand: number | null;
  currentStreak: number | null;
  weeklyXp: number | null;
};
```

---

## 12. Kết luận

Frontend nên implement profile theo đúng 2 tầng dữ liệu:

* `summary` cho header dropdown
* `full detail` cho trang profile

Hướng này giúp:

* header tải nhanh hơn
* giảm gọi API nặng không cần thiết
* UI rõ trách nhiệm hơn
* dễ mở rộng sau này khi thêm achievements hoặc public profile

Nếu cần mở rộng tiếp, tài liệu sau có thể bổ sung:

* wireframe UI chi tiết từng section
* component props proposal
* checklist test cases cho FE
