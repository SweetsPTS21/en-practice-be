Dưới đây là **kế hoạch đề xuất tính năng User Profile kiểu Duolingo** cho project web học từ vựng và IELTS 4 kỹ năng được xây dựng bằng **Spring Boot + ReactJS + PostgreSQL**.

---

# Đề xuất xây dựng User Profile cho nền tảng học từ vựng & IELTS

## 1. Mục tiêu

Xây dựng **User Profile** theo hướng giống Duolingo để:

* tăng động lực học tập
* cá nhân hóa trải nghiệm người học
* theo dõi tiến độ rõ ràng
* tạo cảm giác “game hóa”
* giữ chân người dùng lâu dài

User Profile không chỉ là nơi lưu thông tin cá nhân mà còn là **trung tâm hiển thị hành trình học tập**, thành tích, streak, level, kỹ năng mạnh/yếu và mục tiêu IELTS.

---

# 2. Mục tiêu nghiệp vụ

## 2.1. Đối với người dùng

Người dùng có thể:

* xem thông tin cá nhân
* theo dõi tiến độ học từ vựng và 4 kỹ năng IELTS
* xem chuỗi ngày học liên tiếp
* nhận huy hiệu, điểm kinh nghiệm
* biết kỹ năng nào đang yếu để cải thiện
* đặt mục tiêu band IELTS và kế hoạch học

## 2.2. Đối với hệ thống

Hệ thống có thể:

* lưu trữ lịch sử học tập
* phân tích hành vi học
* đề xuất bài học phù hợp
* tạo cơ chế giữ chân người dùng qua streak, reward, rank
* hỗ trợ dashboard cho admin hoặc mentor sau này

---

# 3. Định hướng thiết kế giống Duolingo

Các đặc điểm nên học theo Duolingo:

## 3.1. Hồ sơ đơn giản nhưng giàu động lực

Profile không quá nặng về thông tin cá nhân, tập trung vào:

* avatar
* tên hiển thị
* level
* XP
* streak
* badges
* thống kê học tập

## 3.2. Trực quan hóa tiến độ

Dùng progress bar, radar chart, milestone chart để hiển thị:

* từ vựng đã học
* bài học đã hoàn thành
* band ước lượng từng kỹ năng
* chuỗi học tập

## 3.3. Game hóa

Thêm các thành phần:

* XP
* level
* streak
* achievements
* weekly goals
* ranking
* daily challenge

## 3.4. Cá nhân hóa

Profile nên thể hiện:

* mục tiêu IELTS của người học
* trình độ hiện tại
* kỹ năng yếu nhất
* đề xuất nội dung tiếp theo

---

# 4. Các module chính của User Profile

## 4.1. Thông tin cá nhân cơ bản

Bao gồm:

* avatar
* username
* full name
* email
* ngày tham gia
* bio ngắn
* mục tiêu học tập

### Gợi ý hiển thị

* Avatar tròn
* Nút Edit Profile
* Join date
* Mục tiêu hiện tại: “Target IELTS 6.5”

---

## 4.2. Học lực và tiến độ tổng quan

Hiển thị ngay đầu profile:

* tổng XP
* current level
* streak hiện tại
* số bài đã hoàn thành
* tổng số từ đã học
* thời gian học tích lũy

### Ví dụ

* XP: 12,450
* Level: 18
* Streak: 21 ngày
* Words learned: 860
* Lessons completed: 143
* Study time: 52 giờ

---

## 4.3. Tiến độ theo 4 kỹ năng IELTS

Mỗi kỹ năng nên có 1 khối riêng:

* Listening
* Reading
* Writing
* Speaking

Với từng kỹ năng, hiển thị:

* level hoặc band ước lượng
* số bài đã học
* tỷ lệ hoàn thành
* điểm trung bình các bài luyện
* nhận xét ngắn

### Ví dụ

**Listening**

* Estimated Band: 6.0
* Completed: 32 lessons
* Accuracy: 78%
* Recommendation: luyện Part 3 và Section 4

---

## 4.4. Tiến độ học từ vựng

Phần này là điểm mạnh nếu website tập trung vào vocab.

Nên hiển thị:

* tổng số từ đã học
* số từ đã master
* số từ cần ôn lại
* số từ sai nhiều
* phân loại theo chủ đề
* biểu đồ học từ theo tuần/tháng

### Nên có

* “Words mastered”
* “Review needed”
* “Weak words”
* “Vocabulary by topic”

---

## 4.5. Streak và thói quen học

Giống Duolingo, đây là phần rất quan trọng.

Hiển thị:

* streak hiện tại
* streak dài nhất
* lịch sử học 30 ngày gần đây
* số ngày học trong tuần
* goal completion rate

### UI gợi ý

* heatmap như GitHub
* icon lửa cho streak
* câu nhắc động lực:

  * “Bạn chỉ còn 1 ngày để phá kỷ lục streak”
  * “Hôm nay bạn chưa hoàn thành mục tiêu”

---

## 4.6. Achievement / Badge

Tạo hệ thống huy hiệu để tăng động lực.

Ví dụ:

* First Lesson Completed
* 7-Day Streak
* 100 Words Learned
* Listening Master
* Writing Warrior
* IELTS Goal Setter
* Vocabulary Hunter

### Cách hiển thị

* badge grid
* badge locked/unlocked
* tooltip giải thích điều kiện mở khóa

---

## 4.7. Mục tiêu cá nhân

User có thể thiết lập:

* target IELTS band
* target date
* daily study goal
* weekly word target
* kỹ năng muốn ưu tiên

### Ví dụ

* Target band: 6.5
* Exam date: 30/09/2026
* Daily goal: 30 phút/ngày
* Weekly target: 80 từ mới

---

## 4.8. Hoạt động gần đây

Hiển thị timeline như:

* hoàn thành lesson Reading True/False/Not Given
* học 20 từ chủ đề Education
* đạt badge 7-Day Streak
* hoàn thành mock test Listening

Mục tiêu là tạo cảm giác tài khoản “sống” và có tiến triển.

---

## 4.9. Ranking / Social nhẹ

Nếu muốn giống Duolingo hơn, có thể thêm:

* bảng xếp hạng tuần
* so sánh XP với bạn bè
* follow người dùng khác
* share achievement

Nếu giai đoạn đầu chưa làm social đầy đủ thì chỉ cần:

* weekly leaderboard
* top learners this week

---

# 5. Đề xuất cấu trúc trang User Profile

## 5.1. Header profile

Bao gồm:

* avatar
* tên người dùng
* level
* streak
* target IELTS band
* nút edit

## 5.2. Overview cards

4-6 card thống kê:

* XP
* lessons completed
* words learned
* total study time
* streak
* completed goals

## 5.3. Skill progress section

Dùng card hoặc chart cho 4 kỹ năng.

## 5.4. Vocabulary dashboard

Biểu đồ và danh sách từ vựng theo trạng thái.

## 5.5. Achievements

Grid badge.

## 5.6. Recent activities

Timeline.

## 5.7. Learning recommendations

Ví dụ:

* “Bạn đang yếu Writing Task 2”
* “Nên ôn lại 35 từ vựng chủ đề Environment”
* “Đề xuất bài Listening Intermediate 05”

---

# 6. Chức năng chi tiết cần phát triển

## 6.1. Chức năng cho người dùng

* xem profile cá nhân
* sửa thông tin cá nhân
* đổi avatar
* thiết lập mục tiêu học
* xem tiến độ kỹ năng
* xem lịch sử học
* xem badge
* xem thống kê từ vựng

## 6.2. Chức năng hệ thống

* tự động cộng XP
* tự động tính streak
* tự động mở khóa badge
* tính level từ XP
* phân tích độ chính xác theo kỹ năng
* đề xuất lesson tiếp theo

---

# 7. Đề xuất dữ liệu cần lưu

## 7.1. Bảng users

* id
* username
* full_name
* email
* password_hash
* avatar_url
* bio
* created_at
* updated_at

## 7.2. Bảng user_profiles

* user_id
* target_ielts_band
* target_exam_date
* daily_goal_minutes
* weekly_word_goal
* preferred_skill

### Ghi chú thiết kế backend

Không nên lưu `total_xp`, `current_level`, `current_streak`, `longest_streak` trực tiếp trong `user_profiles` ở giai đoạn đầu.

Lý do:

* `total_xp` đã có sẵn trong bảng `users`
* `current_level` nên được tính động từ `users.total_xp`
* `current_streak` và `longest_streak` nên được tổng hợp từ lịch sử học hoặc bảng log/snapshot

Vì vậy `user_profiles` nên chỉ chứa dữ liệu người dùng tự cấu hình hoặc chỉnh sửa.

## 7.3. Bảng user_learning_stats

* user_id
* total_words_learned
* total_words_mastered
* total_lessons_completed
* total_study_minutes
* listening_score_avg
* reading_score_avg
* writing_score_avg
* speaking_score_avg

## 7.4. Bảng user_skill_progress

* id
* user_id
* skill_type
* estimated_band
* completed_lessons
* accuracy_rate
* updated_at

## 7.5. Bảng achievements

* id
* code
* name
* description
* icon

## 7.6. Bảng user_achievements

* id
* user_id
* achievement_id
* unlocked_at

## 7.7. Bảng daily_learning_logs

* id
* user_id
* learned_date
* study_minutes
* words_learned
* lessons_completed
* xp_earned

## 7.8. Bảng vocabulary_progress

* id
* user_id
* word_id
* mastery_level
* correct_count
* wrong_count
* next_review_at
* last_reviewed_at

---

# 8. Công thức game hóa đề xuất

## 8.1. XP

Ví dụ:

* hoàn thành lesson vocab: +10 XP
* hoàn thành lesson listening: +20 XP
* hoàn thành writing task: +30 XP
* học liên tiếp mỗi ngày: bonus +5 XP
* hoàn thành daily goal: bonus +15 XP

## 8.2. Level

Công thức đơn giản:

* Level 1: 0 XP
* Level 2: 100 XP
* Level 3: 250 XP
* Level 4: 450 XP

Hoặc dùng công thức tăng dần để tạo cảm giác thử thách.

### Đề xuất chính thức cho backend hiện tại

Nên xây dựng `level` dựa trực tiếp trên `users.total_xp` thay vì tạo cột level riêng trong database.

### Nguồn dữ liệu

* `users.total_xp` là source of truth
* `user_xp_logs` dùng để truy vết lịch sử cộng XP
* level được tính động trong service layer

### Công thức level đề xuất

Để khớp với ví dụ Level 2 = 100 XP, Level 3 = 250 XP, Level 4 = 450 XP, có thể dùng ngưỡng tích lũy như sau:

* XP tối thiểu để đạt Level `n`:
* `requiredXp(n) = 0`, nếu `n = 1`
* `requiredXp(n) = 50 * (n * (n + 1) / 2 - 1)`, nếu `n >= 2`

Ví dụ:

* Level 1: từ 0 XP
* Level 2: từ 100 XP
* Level 3: từ 250 XP
* Level 4: từ 450 XP
* Level 5: từ 700 XP
* Level 6: từ 1000 XP

Điểm mạnh của công thức này:

* dễ implement trong Java
* tăng độ khó đều theo từng level
* phù hợp cơ chế game hóa kiểu Duolingo
* không cần migration DB để lưu level

### Thông tin level nên trả về cho frontend

Profile API không nên chỉ trả `level`, mà nên trả đủ progress để frontend hiển thị thanh tiến độ:

* `totalXp`
* `currentLevel`
* `currentLevelMinXp`
* `nextLevel`
* `nextLevelMinXp`
* `xpIntoCurrentLevel`
* `xpNeededForNextLevel`
* `progressPercentage`

### Ví dụ response

```json
{
  "totalXp": 380,
  "currentLevel": 3,
  "currentLevelMinXp": 250,
  "nextLevel": 4,
  "nextLevelMinXp": 450,
  "xpIntoCurrentLevel": 130,
  "xpNeededForNextLevel": 70,
  "progressPercentage": 65
}
```

### Cách tổ chức code backend

Nên tạo một service riêng, ví dụ:

* `LevelService`
* `LevelServiceImpl`

Service này chịu trách nhiệm:

* tính level từ total XP
* tính mốc XP của level hiện tại và level kế tiếp
* trả về object summary để tái sử dụng ở dashboard, profile, leaderboard

### DTO đề xuất

Có thể tạo DTO riêng như:

* `LevelInfoResponse`

Bao gồm:

* `int totalXp`
* `int currentLevel`
* `int currentLevelMinXp`
* `int nextLevel`
* `int nextLevelMinXp`
* `int xpIntoCurrentLevel`
* `int xpNeededForNextLevel`
* `int progressPercentage`

### Luồng tích hợp với codebase hiện tại

1. `XpService` tiếp tục cập nhật `users.total_xp` như hiện tại.
2. `LevelService` đọc `users.total_xp` để tính level.
3. `UserProfileService` và `UserDashboardService` có thể reuse `LevelService`.
4. Frontend chỉ cần render progress bar từ response đã tính sẵn.

### Lợi ích của hướng này

* không trùng dữ liệu
* tránh lệch giữa `total_xp` và `level`
* dễ đổi công thức level sau này
* dễ test unit

## 8.3. Streak

Tăng streak khi user có hoạt động học trong ngày:

* học ít nhất 1 lesson
* hoặc đạt tối thiểu 10 phút học
* hoặc hoàn thành daily goal

## 8.4. Badge

Mở khóa theo mốc:

* 10 lessons
* 50 lessons
* 100 words
* 500 words
* 7 ngày streak
* 30 ngày streak

---

# 9. Gợi ý API backend với Spring Boot

## 9.1. API profile

* `GET /api/profile/me`
* `PUT /api/profile/me`
* `POST /api/profile/avatar`

## 9.2. API stats

* `GET /api/profile/stats`
* `GET /api/profile/skills`
* `GET /api/profile/vocabulary-stats`
* `GET /api/profile/activities`

## 9.3. API achievement

* `GET /api/profile/achievements`

## 9.4. API goals

* `GET /api/profile/goals`
* `PUT /api/profile/goals`

## 9.5. API streak / xp

* `GET /api/profile/progress-summary`

### API level nên có trong profile summary

Trong thực tế có thể không cần tách API riêng cho level. Nên nhúng thẳng level summary vào:

* `GET /api/user/profile`
* hoặc `GET /api/user/profile/summary`

Nếu muốn tách nhỏ để frontend tái sử dụng:

* `GET /api/user/profile/level`

---

# 10. Gợi ý UI frontend với ReactJS

## 10.1. Component structure

* `ProfileHeader`
* `ProfileStatsCards`
* `SkillProgressSection`
* `VocabularyOverview`
* `StreakCalendar`
* `AchievementGrid`
* `RecentActivityTimeline`
* `GoalCard`
* `RecommendationPanel`

## 10.2. Thư viện nên dùng

* Material UI hoặc Ant Design
* Recharts / Chart.js cho biểu đồ
* React Query để fetch data
* Redux Toolkit hoặc Zustand nếu cần state global
* React Hook Form cho form edit profile

## 10.3. UX nên chú ý

* mobile responsive
* loading skeleton
* progress animation
* badge hover effect
* màu sắc tươi sáng, tạo động lực

---

# 11. Lộ trình triển khai đề xuất

## Giai đoạn 1: MVP

Mục tiêu: có profile cơ bản và thống kê học tập

Bao gồm:

* thông tin cá nhân
* XP, level, streak
* tổng số từ đã học
* tổng số lesson đã hoàn thành
* tiến độ 4 kỹ năng cơ bản
* edit profile

### Hạng mục backend nên làm ngay trong MVP

* thêm `avatar_url`, `bio` vào `users`
* tạo bảng `user_profiles` để lưu goal cá nhân
* tạo `LevelService` tính level từ `users.total_xp`
* trả `levelInfo` trong API profile
* reuse dữ liệu từ dashboard, xp, dictionary, activity log

## Giai đoạn 2: Gamification

Bao gồm:

* badge
* streak calendar
* daily goal
* weekly target
* activity timeline

## Giai đoạn 3: Cá nhân hóa

Bao gồm:

* recommendation engine
* estimated IELTS band per skill
* từ vựng cần ôn lại
* kỹ năng yếu cần ưu tiên

## Giai đoạn 4: Social / Ranking

Bao gồm:

* leaderboard
* follow bạn bè
* share achievement

---

# 12. Lợi ích của tính năng này

## 12.1. Với sản phẩm

* tăng retention
* tăng thời gian sử dụng
* tăng tỷ lệ quay lại hằng ngày
* tạo khác biệt với website học truyền thống

## 12.2. Với người học

* thấy rõ tiến bộ
* có động lực học đều hơn
* có lộ trình rõ ràng đến mục tiêu IELTS
* học tập mang tính thử thách và thú vị hơn

---

# 13. Kết luận

User Profile kiểu Duolingo rất phù hợp với project học từ vựng và IELTS 4 kỹ năng. Đây không nên chỉ là trang “thông tin cá nhân”, mà nên là một **bảng điều khiển học tập cá nhân hóa**, kết hợp:

* hồ sơ cá nhân
* tiến độ học tập
* game hóa
* mục tiêu IELTS
* gợi ý cải thiện

Với stack **Spring Boot + ReactJS + PostgreSQL**, tính năng này hoàn toàn khả thi và nên triển khai theo từng giai đoạn, bắt đầu từ **MVP đơn giản nhưng đo lường được hiệu quả học tập**.

Tôi có thể viết tiếp cho bạn bản này theo dạng **proposal chuẩn để nộp giảng viên/khách hàng**, hoặc chuyển thành **SRS + database schema + API design**.
