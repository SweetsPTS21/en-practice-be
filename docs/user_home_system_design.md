# Tài liệu Thiết kế Hệ thống - Màn hình User Home (System Design)

## 1. Tổng quan (Overview)

Màn hình User Home là điểm chạm đầu tiên quan trọng nhất của người dùng sau khi đăng nhập. Nguyên tắc thiết kế chính yếu để tối ưu hóa trải nghiệm và tăng retention:

- **Clear progress:** Thể hiện tiến độ học tập một cách tuyệt đối rõ ràng.
- **Quick start:** Dễ dàng bắt đầu hoặc quay lại bài học chỉ với 1 click.
- **Daily motivation:** Tạo động lực học tập mỗi ngày, làm cho người dùng cảm nhận được "I am improving" (Tôi đang tiến bộ).

### UX Flow Tiêu Chuẩn:

`Mở Website` $\rightarrow$ `Xem bảng mục tiêu hôm nay` $\rightarrow$ `Click "Continue Learning"` $\rightarrow$ `Luyện tập nhanh (Quick Practice)` $\rightarrow$ `Hoàn thành Daily Tasks`
_(Mục tiêu: Đạt được một chu trình luyện tập hoàn chỉnh chỉ trong 5 phút)._

---

## 2. Phân chia Lộ trình Thay đổi (Phases)

Hệ thống được chia làm 3 Phase để đảm bảo phát hành sớm (Go-live MVP) với các tính năng cốt lõi tạo thói quen, sau đó nâng cấp bằng công nghệ phân tích và cá nhân hóa.

### Phase 1: MVP (Minimum Viable Product)

_Mục tiêu: Xây dựng thói quen học tập cơ bản, giữ chân người dùng qua chỉ số trực quan._

- **Today Learning Panel:** Cụm thiết kế chính trên cùng. Hiển thị mục tiêu thời gian trong ngày (vd: 18/30 phút), thanh tiến trình (Progress bar), CTA chính "Continue learning" cùng các nút chuyển nhanh (Vocabulary, Listening, Reading, Speaking).
- **Streak System:** Hệ thống điểm danh chuỗi ngày học liên tiếp (Ví dụ: 🔥 12 Day Streak). Hiển thị dưới dạng lịch Heatmap tương tự GitHub.
- **Daily Tasks:** Widget hiển thị checklist nhiệm vụ học tập mỗi ngày (Ví dụ: Review 20 từ vựng, Nghe 1 bài mini test). Kèm theo hệ thống phần thưởng khi hoàn thành (+XP, +1 streak).
- **Progress Overview:** Biểu đồ thể hiện tiến bộ trực quan:
  - Band Progress (Line chart): Theo dõi sự thay đổi điểm IELTS các kỹ năng trong 30 ngày qua (vd: Listening 6.0 $\rightarrow$ 6.5).
  - Vocabulary Progress (Progress bar/Chart): Tổng hợp số từ đã học, đã thành thạo (mastered), và đang ôn tập (reviewing).
- **Quick Practice (Micro-learning):** Cung cấp các bài tập giải quyết gọn trong 3-5 phút (nghe nhanh, lướt flashcard 5 từ, trả lời nhanh 1 câu speaking), tối ưu cho thời gian nghỉ ngơi (break time, bus, uống cafe).
- **Vocabulary Daily Learning:** Luyện tập từ vựng nổi bật của ngày với khả năng lật thẻ (flip card), nghe phát âm, xem ví dụ và lưu trữ từ khóa.
- **Recent Activity:** Giao diện xem lại các hoạt động làm bài kiểm tra và điểm số gần nhất.

### Phase 2: Cá nhân hóa & Phân tích (AI & Insights)

_Mục tiêu: Phân tích đi sâu điểm yếu của học viên và tự động hoá tư vấn ôn tập._

- **Weak Skills Analysis:** Hệ thống dùng log làm bài để tìm và cảnh báo các dạng kỹ năng yếu của người dùng (Ví dụ: Đọc hay trễ giờ, làm sai True/False/Not Given). Đi kèm nút "Practice now" giải quyết trực diện kỹ năng đó.
- **AI Recommended Practice:** Đề xuất bài tập vừa sức và hữu ích nhất dựa trên trình độ kỹ năng hiện tại (Difficulty: Medium, Thời gian: 8 phút).
- **Weekly Report:** Báo cáo tuần tổng quát về tổng thời gian học, số từ vựng mới thu nạp, các test đã hoàn tất, và chỉ số tiến bộ tổng (VD: Band improvement +0.3).
- **Smart Reminder & Notification Widget:** Banner thông báo khi người dùng ngắt chuỗi 2 ngày chưa tập. Sidebar widget hiển thị các practice mới có sẵn hoặc feedback bài luận/speaking từ giáo viên.

### Phase 3: Gamification & Social

_Mục tiêu: Đẩy mạnh sự gắn kết dài hạn, kích thích phong trào nâng cao và tỷ lệ mở ứng dụng (Retention hook)._

- **Achievement System:** Hệ thống thành tựu thưởng badgets/khe thành công cho học viên ( VD: Đạt mục tiêu 500 từ vựng, Vượt hơn 10 bài nghe).
- **Leaderboard (Social Proof):** Bảng xếp hạng thi đua top học viên cày cuốc trong tuần.
- **Study Streak Freeze:** Cho phép skip 1 ngày hoạt động học trong trường hợp khẩn mà không bị mất chuỗi Streak (Có thể dùng XP để đổi/mua).
- **Vocabulary Memory System:** Tích hợp bộ thuật toán lặp lại ngắt quãng (Spaced Repetition) cho các danh mục ôn luyện từ.
- **AI Speaking Partner:** Bot đối thoại giả lập môi trường thi Speaking/Giao tiếp.

---

## 3. Thiết kế Frontend (FE Architecture)

### 3.1. Cấu trúc Layout Component

Dashboard được thiết kế theo dạng Widget Module dễ tái sử dụng:

- `DashboardLayout` (Main Container)
  - `Header` (Navigation, Avatar, Thông báo)
  - `TodayLearningPanel` (Banner To - Progress Bar, Mục tiêu hằng ngày)
  - `MainGridContainer` (Auto-fill dựa theo kích cỡ Desktop/Tablet/Mobile):
    - Biểu đồ tiến độ (`BandProgressChart`, `VocabularyProgressChart`)
    - AI Insights (`WeakSkillsWidget`, `RecommendedCard`)
    - Cột tương tác chớp nhoáng (`QuickPracticeWidget`, `DailyTasksList`)
    - Cột Tracking (`StreakHeatmapWidget`, `RecentActivityList`)

### 3.2. Thư viện UI Đề xuất

- **Charts:** Sử dụng **Recharts** hoặc **Nivo**. Có tính chất render linh hoạt, dễ tùy chỉnh UI/UX responsive.
- **State Management:** Sử dụng **Zustand** hoặc **Context API + React Query** để cache kết quả API Dashboard, đảm bảo navigation mượt mà không tải lại khung nhìn nhiều lần. Dữ liệu Streak/Hoàn thành task nên được mutate/optimistic update để tăng cảm giác thời gian thực.

---

## 4. Thiết kế Backend (BE Architecture)

### 4.1. Thông tin Dataset Aggregate (API Layer)

Để tối giản độ trễ tải trang màn hình Home, API cung cấp sẽ Aggregate (tổng hợp) toàn bộ thông tin quan trọng.

- **API (RESTful):** `GET /api/v1/user/dashboard`
- **Response Shape (Ví dụ):**

```json
{
  "streak": { "current": 12, "longest": 30, "heatmap": [ ... ] },
  "todayGoal": { "targetMins": 30, "achievedMins": 18 },
  "dailyTasks": {
    "progress": "3/4",
    "items": [
      { "id": 101, "title": "Review 20 vocabulary", "type": "VOCAB", "status": "COMPLETED" },
      { "id": 102, "title": "Listening mini test", "type": "LISTENING", "status": "PENDING" }
    ]
  },
  "progressOverview": {
    "bandProgress": {
      "listening": { "old": 6.0, "current": 6.5 },
      "reading": { "old": 5.5, "current": 6.0 },
      "writing": { "old": 5.0, "current": 5.5 },
      "speaking": { "old": 5.5, "current": 6.0 }
    },
    "vocabulary": { "total": 820, "mastered": 420, "reviewing": 120 }
  },
  "aiInsights": {
    "weakSkills": ["TRUE_FALSE_NOT_GIVEN", "MAP_LABELING", "ACADEMIC_VOCAB"],
    "recommended": [ ... ]
  },
  "recentActivity": [ ... ]
}
```

### 4.2. Database Design & Optimize (Dữ liệu nền)

- `users`: Bổ sung fields như `current_streak`, `longest_streak`, `today_goal`, `timezone`, `last_study_date` (để trigger bảo tồn streak freeze).
- `user_daily_tasks`: Entity sinh ra công việc ngày mới (Cronjob tạo hàng ngày, hoặc Just-in-time sinh ra cho phiên đầu tiên trong ngày user đăng nhập).
- `user_study_logs`: Entity hạt nhân theo dõi Time-spent cho mọi tính năng practice từ Speaking/Vocab, xử lý query cho Chart và Heatmap streak.
- `user_skill_stats`: Tracking số chỉ số đúng/sai của dạng test bài cụ thể thay cho việc count realtime từ log, giúp Backend suy ra Weak Skills rất nhẹ nhàng.

### 4.3. Các Lưu ý Kỹ thuật cho Backend

1. **Caching (Redis):** Dữ liệu `bandProgress`, `weakSkills` là các dữ liệu tổng hợp tốn kém (heavy aggreation). Backend nên thiết lập Caching cho endpoint `/dashboard` hoặc một Cron-job chạy ban đêm tính trước các thông số chart tốn resources (Ví dụ: tính điểm band trung bình 30 ngày qua).
2. **Timezone Accuracy:** Mọi hoạt động log nên ghi dưới dạng chuẩn `UTC`, tính logic Streak (Ví dụ Reset Daily Tasks, Đứt chuỗi) cần phải refer vào Timezone offset mà thiết bị Frontend gửi lên hoặc User Settings.
3. **Decoupled Architecture (Async Jobs):** Nếu ứng dụng có tính năng AI chấm Speaking trả về Band 6.0 hay AI Recommendation, các tiến trình này nên được thực thi bất đồng bộ qua hệ thống Event Queue (Kafka/RabbitMQ) hoặc ThreadPool, sau đó cập nhật log nhằm không đánh dội thời gian chờ trên luồng Web Request chính.
