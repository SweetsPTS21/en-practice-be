# Tài liệu Implement Frontend — Leaderboard & XP System

> Tài liệu dành cho developer thực hiện implement tính năng Leaderboard & XP trên Frontend.
> Đọc kèm: [`leaderboard.md`](./leaderboard.md) (Backend System Design)

---

## 1. Phân tích Kiến trúc Frontend Hiện tại

### 1.1. Technology Stack

| Thành phần | Công nghệ | Version |
|---|---|---|
| Framework | React 19 (Vite 7) | `react@^19.2.0` |
| UI Library | **Ant Design 6** (`antd`) | `antd@^6.3.0` |
| Styling | **Tailwind CSS 4** + Ant Design tokens | `tailwindcss@^4.2.1` |
| Routing | React Router DOM 7 | `react-router-dom@^7.13.1` |
| HTTP Client | Axios (custom `axiosClient.js` with JWT interceptor) | `axios@^1.13.5` |
| Charts | **Recharts** | `recharts@^3.7.0` |
| Animation | **Framer Motion** | `framer-motion@^12.34.3` |
| Icons | `lucide-react` + `@ant-design/icons` | — |
| Date | `dayjs` | `dayjs@^1.11.19` |
| State | React `useState` + `useEffect` + `useCallback` (không dùng Redux/Zustand) | — |

### 1.2. Cấu trúc thư mục (liên quan)

```
src/
├── api/                         → API modules (1 file = 1 domain)
│   ├── axiosClient.js           → Axios instance + JWT interceptor + DefaultResponse unwrap
│   ├── dashboardApi.js          → { getDashboard: () => axiosClient.get(...) }
│   ├── ieltsApi.js              → Mẫu tham khảo đầy đủ
│   ├── speakingApi.js
│   └── index.js                 → Re-export tất cả APIs
├── components/                  → Reusable UI components
│   ├── PageHeader.jsx           → Header gradient cho mỗi page
│   └── ielts/                   → Feature-scoped components (TestCard, TestFilter, ...)
├── contexts/
│   └── AuthContext.jsx          → { user, isAuthenticated, login, logout } via React Context
├── hooks/                       → Custom hooks
├── layouts/
│   └── AdminLayout.jsx          → Layout riêng cho admin
├── pages/
│   ├── dashboard/               → User dashboard widgets
│   │   ├── HomePage.jsx         → Main dashboard page (fetch + compose widgets)
│   │   ├── TodayLearningPanel.jsx
│   │   ├── StreakHeatmap.jsx
│   │   ├── DailyTasksPanel.jsx
│   │   ├── ProgressOverview.jsx → Recharts BarChart
│   │   ├── QuickPracticePanel.jsx
│   │   ├── RecentActivityPanel.jsx
│   │   ├── WeakSkillsWidget.jsx
│   │   ├── RecommendedPracticeWidget.jsx
│   │   └── SmartReminderBanner.jsx
│   ├── ielts/                   → IELTS test pages
│   ├── speaking/                → Speaking pages
│   ├── writing/                 → Writing pages
│   └── admin/                   → Admin pages
├── utils/                       → Helper functions
├── App.jsx                      → Routing + Layout (Sider + Header + Content)
├── main.jsx                     → Entry: ConfigProvider + BrowserRouter + AuthProvider
└── index.css                    → Global styles, Tailwind @theme, glass-card, animations
```

### 1.3. Design Patterns (bắt buộc tuân thủ)

#### API Layer

```javascript
// Pattern: 1 file export 1 object, methods return axiosClient calls
// axiosClient TỰ ĐỘNG unwrap DefaultResponse → trả về `data` field
export const leaderboardApi = {
    getLeaderboard(params = {}) {
        return axiosClient.get('/leaderboard', { params });
    },
    getSummary() {
        return axiosClient.get('/leaderboard/summary');
    },
};
```

#### Page Component

```jsx
// Pattern: Page tự fetch data trong useEffect, truyền data xuống child widgets
export default function LeaderboardPage() {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetch = async () => {
            try {
                const response = await leaderboardApi.getLeaderboard();
                setData(response);
            } catch (error) {
                message.error('...');
            } finally {
                setLoading(false);
            }
        };
        fetch();
    }, []);

    // ...render
}
```

#### Widget/Card Component

```jsx
// Pattern: Nhận props, render UI, dùng glass-card + framer-motion
// Ant Design components + Tailwind utility classes
// lucide-react cho icons
export default function SomeWidget({ data }) {
    return (
        <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="glass-card border-0! rounded-2xl p-6 shadow-xl h-full"
        >
            {/* ... */}
        </motion.div>
    );
}
```

#### Styling Conventions

| Quy tắc | Ví dụ |
|---|---|
| Card wrapper | `className="glass-card border-0! rounded-2xl p-6 shadow-xl h-full"` |
| Gradient header cards | `className="bg-linear-to-br from-indigo-900 to-purple-800 rounded-2xl p-6 text-white shadow-xl"` |
| Title | `<Title level={4} className="text-gray-800! m-0!">` |
| Subtitle | `<Text className="text-gray-500! text-sm">` |
| Ant Design `!important` override | Dùng `!` suffix: `text-gray-800!` (Tailwind v4 syntax) |
| Animation wrapper | `<motion.div initial={{...}} animate={{...}}>` |
| Full-page layout | `className="max-w-[1400px] mx-auto pb-10"` hoặc `"max-w-6xl mx-auto"` |
| Page entry animation | `className="animate-fade-in-up"` |

#### Color Palette (từ `index.css` @theme)

| Token | Value | Dùng cho |
|---|---|---|
| `primary-500` | `#6366f1` (Indigo) | Primary actions, links |
| `primary-600` | `#4f46e5` | Hover states |
| `success` | `#10b981` (Emerald) | Positive indicators |
| `danger` | `#ef4444` (Red) | Errors, warnings |
| `warning` | `#f59e0b` (Amber) | Caution states |
| Gold/Trophy | `#f59e0b` / `#fbbf24` | Rank 1, Gold badge |
| Silver | `#94a3b8` | Rank 2 |
| Bronze | `#d97706` | Rank 3 |

---

## 2. Backend API Contracts (đã implement)

> Frontend gọi qua `axiosClient` → auto unwrap `DefaultResponse` → nhận trực tiếp `data`.

### 2.1. `GET /api/leaderboard` — Bảng xếp hạng

**Params:** `period` (WEEKLY|MONTHLY|ALL_TIME), `scope` (GLOBAL|BY_TARGET_BAND), `targetBand`, `page`, `size`

**Response (đã unwrap):**

```json
{
  "myRank": {
    "rank": 15,
    "totalParticipants": 0,
    "xp": 680,
    "xpToNextRank": 23,
    "rankChange": 3,
    "rankChangeDirection": "UP"
  },
  "topUsers": [
    {
      "rank": 1,
      "userId": "uuid",
      "displayName": "Minh Anh",
      "avatarUrl": null,
      "targetBand": null,
      "xp": 1250,
      "currentStreak": 0,
      "rankChange": 0,
      "rankChangeDirection": "STABLE"
    }
  ],
  "page": {
    "page": 0,
    "totalPages": 5,
    "totalElements": 98
  }
}
```

### 2.2. `GET /api/leaderboard/summary` — Widget Dashboard

**Response:**

```json
{
  "period": "WEEKLY",
  "myRank": { "rank": 15, "xp": 680, "rankChange": 3, "rankChangeDirection": "UP" },
  "topThree": [
    { "rank": 1, "displayName": "Minh Anh", "xp": 1250 }
  ]
}
```

### 2.3. `GET /api/xp/history` — Lịch sử XP

**Params:** `page`, `size`

**Response:**

```json
{
  "totalXP": 2450,
  "weeklyXP": 680,
  "history": [
    {
      "id": "uuid",
      "source": "FULL_TEST_COMPLETE",
      "description": "Earned from FULL_TEST_COMPLETE",
      "xp": 50,
      "earnedAt": "2025-01-15T08:30:00Z"
    }
  ],
  "page": { "page": 0, "totalPages": 5, "totalElements": 98 }
}
```

---

## 3. Files cần tạo

### 3.1. Tổng quan

```
src/
├── api/
│   └── leaderboardApi.js              ← NEW
├── pages/
│   ├── dashboard/
│   │   └── LeaderboardWidget.jsx      ← NEW (widget nhỏ trên HomePage)
│   └── leaderboard/                   ← NEW directory
│       ├── LeaderboardPage.jsx        ← NEW (page chính)
│       ├── MyRankCard.jsx             ← NEW
│       ├── TopThreePodium.jsx         ← NEW
│       ├── LeaderboardTable.jsx       ← NEW
│       ├── XpHistoryPage.jsx          ← NEW
│       └── XpHistoryTimeline.jsx      ← NEW
├── App.jsx                            ← MODIFY (thêm routes)
└── api/
    └── index.js                       ← MODIFY (export leaderboardApi)
```

### 3.2. Sửa files hiện có

| File | Thay đổi |
|---|---|
| `src/App.jsx` | Thêm routes `/leaderboard`, `/xp/history` + menu item |
| `src/api/index.js` | Thêm `export { leaderboardApi }` |
| `src/pages/dashboard/HomePage.jsx` | Thêm `<LeaderboardWidget />` vào grid layout |

---

## 4. Chi tiết Implement từng File

### 4.1. `src/api/leaderboardApi.js`

```javascript
import axiosClient from './axiosClient';

/**
 * Leaderboard & XP API
 */
export const leaderboardApi = {
    /**
     * Lấy bảng xếp hạng
     * @param {{ period?: string, scope?: string, targetBand?: number, page?: number, size?: number }} params
     */
    getLeaderboard(params = {}) {
        return axiosClient.get('/leaderboard', { params });
    },

    /**
     * Lấy summary cho dashboard widget (top 3 + my rank)
     */
    getSummary() {
        return axiosClient.get('/leaderboard/summary');
    },

    /**
     * Lấy lịch sử XP cá nhân
     * @param {{ page?: number, size?: number }} params
     */
    getXpHistory(params = {}) {
        return axiosClient.get('/xp/history', { params });
    },
};
```

### 4.2. `src/api/index.js` — Bổ sung export

```javascript
// Thêm dòng:
export { leaderboardApi } from './leaderboardApi';
```

---

### 4.3. `src/pages/dashboard/LeaderboardWidget.jsx` — Widget trên HomePage

**Mô tả:** Card nhỏ gọn hiển thị top 3 + my rank, nằm trong grid layout của `HomePage.jsx`.

**Mockup:**

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

**Chi tiết implement:**

- Fetch riêng bằng `leaderboardApi.getSummary()` trong `useEffect` (KHÔNG dùng data từ `dashboardApi` vì BE chưa integrate leaderboard vào `DashboardResponse`)
- Loading state: dùng `<Skeleton />` (Ant Design)
- Nếu `summary.myRank === null` → hiển thị "Chưa có thứ hạng, hãy bắt đầu luyện tập!"
- Rank change indicator: ↑ (xanh lá), ↓ (đỏ), ─ (xám), NEW (badge tím)
- Click "View Full Leaderboard →" → `navigate('/leaderboard')`
- Medal icons: 🥇🥈🥉 (dùng emoji hoặc custom SVG)

**Style:** Tuân theo pattern `glass-card` + `motion.div` + `Title level={4}` + gradient bg cho rank badges.

**Props:** Không cần props, tự fetch.

**Vị trí trong `HomePage.jsx`:** Thêm vào grid, đặt ở vị trí `col-span-1 lg:col-span-4` cạnh `DailyTasksPanel` hoặc dưới `StreakHeatmap`.

---

### 4.4. `src/pages/leaderboard/LeaderboardPage.jsx` — Trang chính

**Mô tả:** Full-page hiển thị bảng xếp hạng với filter, podium top 3, bảng danh sách, và rank cá nhân.

**Layout Structure:**

```
LeaderboardPage (max-w-[1400px] mx-auto)
├── PageHeader (icon=Trophy, title="Leaderboard")
├── FilterBar (Tabs: Weekly|Monthly|All-Time + Dropdown: Global|By Band)
├── Grid Layout (lg:grid-cols-12)
│   ├── MyRankCard (col-span-4)
│   ├── TopThreePodium (col-span-8)
│   └── LeaderboardTable (col-span-12)
```

**Chi tiết implement:**

- State: `period` (default `'WEEKLY'`), `scope` (default `'GLOBAL'`), `page` (default 0), `data`, `loading`
- Fetch khi `period`, `scope`, hoặc `page` thay đổi → `leaderboardApi.getLeaderboard({ period, scope, page, size: 20 })`
- Filter Tabs: dùng `<Segmented />` (Ant Design) cho period selector — style giống IELTS TestFilter
- Scope dropdown: `<Select />` (Ant Design)
- Pagination: `<Pagination />` (Ant Design), map `page+1` ↔ `page` (0-indexed API)
- Guard: `if (!isAuthenticated) return <Navigate to="/login" />`

**Period Tab labels:**

| Key | Label | Icon |
|---|---|---|
| `WEEKLY` | This Week | `Calendar` (lucide) |
| `MONTHLY` | This Month | `CalendarDays` (lucide) |
| `ALL_TIME` | All Time | `Infinity` (lucide) |

---

### 4.5. `src/pages/leaderboard/MyRankCard.jsx`

**Props:** `myRank` object (from API response) hoặc `null`

**Mô tả:** Card hiển thị rank cá nhân user, XP hiện tại, khoảng cách tới rank kế tiếp.

**Mockup:**

```
┌────────────────────────┐
│  Your Ranking           │
│                         │
│  #15                    │
│  ──────                 │
│  680 XP earned          │
│  23 XP to rank #14      │
│                         │
│  ↑3 from last snapshot  │
│  ──────                 │
│  [View XP History →]    │
└────────────────────────┘
```

**Chi tiết:**

- Rank number: font-size lớn (`text-5xl font-bold`), gradient text `from-indigo-500 to-purple-500`
- Rank change indicator:
  - `UP` → `<ArrowUp className="text-emerald-500" />` + `text-emerald-600`
  - `DOWN` → `<ArrowDown className="text-red-500" />` + `text-red-600`
  - `STABLE` → `<Minus className="text-gray-400" />`
  - `NEW` → `<Tag color="purple">NEW</Tag>`
- XP to next rank: progress bar nhỏ hoặc text
- CTA: `<Button>` navigate to `/xp/history`
- Nếu `myRank === null` → hiển thị placeholder "Chưa có thứ hạng"

**Style:** `glass-card`, `h-full`

---

### 4.6. `src/pages/leaderboard/TopThreePodium.jsx`

**Props:** `topUsers` array (lấy 3 phần tử đầu)

**Mô tả:** Podium visualization hiển thị top 3 users kiểu Olympic.

**Layout:**

```
        🥇 #1
      ┌───────┐
  🥈  │       │  🥉
┌─────┤       ├─────┐
│ #2  │  #1   │ #3  │
└─────┴───────┴─────┘
```

**Chi tiết:**

- Layout: `flex items-end justify-center gap-4`
- Mỗi podium column:
  - Avatar circle: `<Avatar>` (Ant Design) với initials, size tùy rank (1st lớn nhất)
  - Display name: truncate
  - XP: bold
  - Rank badge: gradient background
- Rank 1 (center): cao nhất, `h-36`, gold gradient `from-yellow-400 to-amber-500`
- Rank 2 (left): `h-28`, silver gradient `from-gray-300 to-gray-400`
- Rank 3 (right): `h-24`, bronze gradient `from-amber-600 to-amber-700`
- Order in flex: `[rank2, rank1, rank3]` để rank 1 ở giữa
- Nếu `topUsers.length < 3` → chỉ render số lượng có
- Animation: mỗi column `motion.div` với `initial={{ y: 50, opacity: 0 }}` staggered

---

### 4.7. `src/pages/leaderboard/LeaderboardTable.jsx`

**Props:** `topUsers` array (full page), `currentUserId` (UUID)

**Mô tả:** Bảng danh sách xếp hạng kiểu table/list.

**Chi tiết:**

- Dùng `<Table />` (Ant Design) hoặc custom list
- Columns:
  - **Rank**: `#1`, `#2`, ... — top 3 hiển thị medal emoji (🥇🥈🥉)
  - **User**: Avatar + Display Name
  - **XP**: số XP, bold, `text-indigo-600`
  - **Change**: RankChangeDirection indicator (arrow + số)
- Highlight row nếu `userId === currentUserId` → `bg-indigo-50 border-l-4 border-indigo-500`
- Alternate row colors nhẹ
- Responsive: trên mobile ẩn một số columns

**Style:** Bên trong `glass-card`, rounded-2xl

---

### 4.8. `src/pages/leaderboard/XpHistoryPage.jsx` — Trang lịch sử XP

**Mô tả:** Full-page hiển thị XP overview + timeline lịch sử.

**Layout:**

```
XpHistoryPage (max-w-4xl mx-auto)
├── PageHeader (icon=Zap, title="XP History")
├── XP Stats Cards (grid 2 cols)
│   ├── Total XP Card (gradient indigo)
│   └── Weekly XP Card (gradient purple)
└── XpHistoryTimeline (paginated)
```

**Chi tiết:**

- Fetch: `leaderboardApi.getXpHistory({ page, size: 20 })`
- XP stat cards: dùng pattern `stat-card-purple` từ `index.css`, icon `<Zap>` (lucide)
- Pagination: `<Pagination />` ở dưới cùng

---

### 4.9. `src/pages/leaderboard/XpHistoryTimeline.jsx`

**Props:** `history` array, `loading`

**Mô tả:** Timeline hiển thị từng XP event.

**Chi tiết:**

- Dùng `<Timeline />` (Ant Design) — pattern giống `RecentActivityPanel.jsx`
- Mỗi item:
  - Icon theo `source` type (map tương tự `getIconForType` pattern hiện có)
  - Title: mô tả activity
  - XP earned: `+50 XP` badge (xanh lá)
  - Timestamp: `dayjs(earnedAt).fromNow()`

**Source → Icon mapping:**

| XpSource | Icon (lucide) | Color |
|---|---|---|
| `FULL_TEST_COMPLETE` | `GraduationCap` | `text-blue-500` |
| `MINI_TEST_COMPLETE` | `ClipboardCheck` | `text-blue-400` |
| `VOCABULARY_REVIEW` | `Book` | `text-indigo-500` |
| `SPEAKING_PRACTICE` | `Mic` | `text-orange-500` |
| `WRITING_SUBMISSION` | `PenLine` | `text-purple-500` |
| `DAILY_TASK_COMPLETE` | `ListChecks` | `text-emerald-500` |
| `ALL_DAILY_TASKS_BONUS` | `Star` | `text-yellow-500` |
| `STREAK_BONUS` | `Flame` | `text-orange-500` |
| `QUICK_PRACTICE` | `Zap` | `text-indigo-400` |
| `BAND_INCREASE` | `TrendingUp` | `text-emerald-600` |
| `WEEKLY_REWARD` | `Trophy` | `text-amber-500` |

**Source → Description mapping (human-readable):**

| XpSource | Description |
|---|---|
| `FULL_TEST_COMPLETE` | Hoàn thành bài thi IELTS |
| `MINI_TEST_COMPLETE` | Hoàn thành bài thi nhanh |
| `VOCABULARY_REVIEW` | Ôn tập từ vựng |
| `SPEAKING_PRACTICE` | Luyện Speaking |
| `WRITING_SUBMISSION` | Nộp bài Writing |
| `DAILY_TASK_COMPLETE` | Hoàn thành nhiệm vụ hàng ngày |
| `ALL_DAILY_TASKS_BONUS` | Bonus hoàn thành tất cả nhiệm vụ |
| `STREAK_BONUS` | Bonus duy trì streak |
| `QUICK_PRACTICE` | Luyện tập nhanh |
| `BAND_INCREASE` | Tăng band score |
| `WEEKLY_REWARD` | Phần thưởng xếp hạng tuần |

---

## 5. Sửa đổi Files Hiện có

### 5.1. `src/App.jsx`

**Thay đổi 1:** Thêm imports

```javascript
import LeaderboardPage from './pages/leaderboard/LeaderboardPage';
import XpHistoryPage from './pages/leaderboard/XpHistoryPage';
```

**Thay đổi 2:** Thêm menu item vào `menuItems` array

```javascript
{
    key: '/leaderboard',
    icon: <Trophy size={16} />,   // import { Trophy } from 'lucide-react'
    label: 'Leaderboard',
},
```

> Đặt sau item `/ielts` trong array `menuItems`.

**Thay đổi 3:** Thêm routes trong `<Routes>`

```jsx
<Route path="/leaderboard" element={<LeaderboardPage />} />
<Route path="/xp/history" element={<XpHistoryPage />} />
```

**Thay đổi 4:** Thêm page title

```javascript
// Trong pageTitles object:
'/leaderboard': 'Bảng Xếp Hạng',
'/xp/history': 'Lịch Sử XP',
```

### 5.2. `src/pages/dashboard/HomePage.jsx`

**Thay đổi:** Thêm `<LeaderboardWidget />` vào grid layout.

```jsx
import LeaderboardWidget from './LeaderboardWidget';

// Trong JSX, thêm vào grid (sau StreakHeatmap hoặc cạnh QuickPracticePanel):
<div className="col-span-1 lg:col-span-4 h-full">
    <LeaderboardWidget />
</div>
```

**Vị trí đề xuất trong grid:**

```
┌─ TodayLearningPanel (8 cols) ─┐  ┌─ StreakHeatmap (4 cols) ─┐
├─ WeakSkills (4 cols) ──────────┤  ├─ Recommended (8 cols) ──┤
├─ DailyTasks (4 cols) ──────────┤  ├─ ProgressOverview (8) ──┤
├─ LeaderboardWidget (4 cols) ◄──┤  ├─ RecentActivity (8) ────┤   ← THÊM VÀO ĐÂY
├─ QuickPractice (4 cols) ───────┤  │                         │
```

---

## 6. Component Specifications Chi Tiết

### 6.1. LeaderboardWidget — Behavior & States

| State | UI |
|---|---|
| Loading | `<Skeleton active paragraph={{ rows: 4 }} />` bên trong glass-card |
| Error | Text "Không thể tải leaderboard" + Retry button |
| Empty (no data) | "Chưa có dữ liệu xếp hạng" + CTA "Bắt đầu luyện tập" |
| No rank (myRank null) | Top 3 vẫn hiển thị, section "Your rank" hiện placeholder |
| Success | Full widget với top 3 + my rank |

### 6.2. LeaderboardPage — Filter UX

```
┌──────────────────────────────────────────────────────────┐
│  [This Week]  [This Month]  [All Time]    Scope: [Global ▾]  │
└──────────────────────────────────────────────────────────┘
```

- Period selector: `<Segmented />` (Ant Design) — rounded, indigo active color
- Scope selector: `<Select />` — chỉ hiển thị khi có dữ liệu By Target Band
- Khi chuyển filter → reset `page = 0`, re-fetch

### 6.3. TopThreePodium — Responsive

| Breakpoint | Layout |
|---|---|
| Desktop (≥ 1024px) | 3 columns, podium style (center taller) |
| Tablet (≥ 768px) | 3 columns, chiều cao giảm |
| Mobile (< 768px) | Horizontal scrollable hoặc stack thành 3 cards ngang |

### 6.4. LeaderboardTable — Column Spec

| Column | Width | Mobile | Mô tả |
|---|---|---|---|
| Rank | 60px | Show | `#1` 🥇, `#2` 🥈, `#3` 🥉, `#4+` plain number |
| User | flex | Show | Avatar initials + display name |
| XP | 100px | Show | Bold, `text-indigo-600` |
| Streak | 80px | Hide | 🔥 + number |
| Change | 80px | Hide | Arrow icon + number |

---

## 7. Routing Configuration

### 7.1. Routes mới

| Path | Component | Auth Required | Menu |
|---|---|---|---|
| `/leaderboard` | `LeaderboardPage` | Yes | ✅ (sidebar) |
| `/xp/history` | `XpHistoryPage` | Yes | ❌ (navigate from LeaderboardPage/MyRankCard) |

### 7.2. Navigation Flow

```
HomePage
  └── LeaderboardWidget
        ├── Click "View Full Leaderboard" → /leaderboard
        └── Click top user → (future: profile page)

LeaderboardPage
  ├── MyRankCard
  │     └── Click "View XP History" → /xp/history
  ├── TopThreePodium (read-only)
  └── LeaderboardTable (pagination)

XpHistoryPage
  └── Back button → /leaderboard
```

---

## 8. Animation Specifications

| Component | Animation | Config |
|---|---|---|
| LeaderboardWidget | Fade in + scale | `initial={{ opacity: 0, scale: 0.95 }}` `animate={{ opacity: 1, scale: 1 }}` |
| TopThreePodium columns | Slide up staggered | `initial={{ y: 50, opacity: 0 }}` `transition={{ delay: index * 0.15, type: 'spring' }}` |
| LeaderboardTable rows | Fade in staggered | `initial={{ opacity: 0, x: -10 }}` `transition={{ delay: index * 0.03 }}` |
| MyRankCard rank number | Count up | Tùy chọn: dùng `framer-motion` `useMotionValue` hoặc đơn giản hiện luôn |
| XP History items | Timeline fade in | Giống `RecentActivityPanel` pattern |
| Rank change badge | Pulse | `animate={{ scale: [1, 1.1, 1] }}` `transition={{ repeat: 2, duration: 0.3 }}` |

---

## 9. Error Handling

| Scenario | Xử lý |
|---|---|
| 401 Unauthorized | `axiosClient` interceptor tự redirect → login |
| Network error | `message.error('Không thể kết nối server')` |
| Empty leaderboard | Hiển thị Empty state với illustration |
| API trả data rỗng `topUsers: []` | TopThreePodium ẩn, Table hiện Empty |
| `myRank: null` | MyRankCard hiện placeholder |

---

## 10. Lộ trình Implement Frontend

| Step | Task | Ước tính |
|---|---|---|
| **1** | Tạo `leaderboardApi.js` + export trong `index.js` | 15 phút |
| **2** | Tạo `LeaderboardWidget.jsx` + tích hợp vào `HomePage.jsx` | 2 giờ |
| **3** | Tạo `LeaderboardPage.jsx` (layout + filter + fetch) | 2 giờ |
| **4** | Tạo `MyRankCard.jsx` | 1 giờ |
| **5** | Tạo `TopThreePodium.jsx` | 2 giờ |
| **6** | Tạo `LeaderboardTable.jsx` | 1.5 giờ |
| **7** | Tạo `XpHistoryPage.jsx` + `XpHistoryTimeline.jsx` | 2 giờ |
| **8** | Cập nhật `App.jsx` (routes + menu) | 30 phút |
| **9** | Responsive testing + polish animations | 2 giờ |
| **10** | Edge cases: empty states, error handling, loading skeletons | 1 giờ |

**Tổng ước tính: ~14 giờ (~2 ngày làm việc)**

---

## 11. Checklist QA

- [ ] `leaderboardApi.js` gọi đúng endpoints, params mapping chính xác
- [ ] `LeaderboardWidget` fetch + render trên HomePage, loading/error/empty states
- [ ] `LeaderboardPage` filter period/scope hoạt động, pagination hoạt động
- [ ] `TopThreePodium` render đúng thứ tự (2-1-3), responsive mobile
- [ ] `LeaderboardTable` highlight current user row, medal icons top 3
- [ ] `MyRankCard` hiển thị rank change direction đúng (UP=xanh, DOWN=đỏ)
- [ ] `XpHistoryPage` pagination hoạt động, icon mapping đúng source
- [ ] Routes `/leaderboard` và `/xp/history` accessible từ sidebar + navigation
- [ ] Auth guard: redirect to login nếu chưa authenticated
- [ ] Mobile responsive: tất cả components hiển thị tốt trên < 768px
- [ ] Animations smooth, không giật
- [ ] Console không có errors/warnings

