# Thiết Kế Chức Năng Từ Điển (User Dictionary)

Cho phép người dùng lưu trữ các từ vựng yêu thích vào **từ điển riêng** của mình. Khác với `vocabulary_records` hiện tại (dùng cho review session), bảng mới này tập trung vào việc người dùng **chủ động thêm**, **tra cứu** và **quản lý** từ vựng cá nhân.

---

## 1. Database Schema

### Bảng mới: `user_dictionary`

```sql
CREATE TABLE user_dictionary (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Từ vựng
    word            VARCHAR(200) NOT NULL,
    ipa             VARCHAR(200),                   -- Phiên âm: /ˈæp.əl/
    word_type       VARCHAR(50),                    -- noun, verb, adjective, adverb, ...

    -- Nghĩa & giải thích
    meaning         TEXT NOT NULL,                  -- Nghĩa tiếng Việt (bắt buộc)
    explanation     TEXT,                           -- Giải thích thêm (tiếng Anh hoặc Việt)
    note            TEXT,                           -- Lưu ý riêng của người dùng

    -- Ví dụ (lưu dạng JSONB để hỗ trợ nhiều ví dụ)
    examples        JSONB DEFAULT '[]',             -- [{"sentence": "...", "translation": "..."}]

    -- Metadata
    is_favorite     BOOLEAN NOT NULL DEFAULT FALSE, -- Đánh dấu yêu thích đặc biệt
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Ngăn trùng lặp từ cùng user
    CONSTRAINT uq_user_word UNIQUE (user_id, word)
);

CREATE INDEX idx_user_dictionary_user_id ON user_dictionary(user_id);
CREATE INDEX idx_user_dictionary_word    ON user_dictionary(user_id, word);
```

> [!NOTE]
> Trường `examples` dùng kiểu `JSONB` để linh hoạt lưu nhiều ví dụ với cả câu gốc lẫn bản dịch, tương tự pattern đã dùng trong `VocabularyRecord`.

---

## 2. Entity

### `UserDictionary.java`

| Field | Type | Column | Ghi chú |
|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto-generated |
| `userId` | `UUID` | `user_id` | FK → `users` |
| `word` | `String` | `word` | Từ tiếng Anh, NOT NULL |
| `ipa` | `String` | `ipa` | Phiên âm quốc tế |
| `wordType` | `String` | `word_type` | Từ loại: noun, verb, … |
| `meaning` | `String` | `meaning` | Nghĩa tiếng Việt, NOT NULL |
| `explanation` | `String` | `explanation` | Giải thích mở rộng |
| `note` | `String` | `note` | Ghi chú của user |
| `examples` | `List<ExampleSentence>` | `examples` | JSONB list |
| `isFavorite` | `Boolean` | `is_favorite` | Đánh dấu ưa thích |
| `createdAt` | `Instant` | `created_at` | Auto `@PrePersist` |
| `updatedAt` | `Instant` | `updated_at` | Auto `@PreUpdate` |

#### Nested DTO dùng cho JSONB: `ExampleSentence`
```java
public class ExampleSentence {
    private String sentence;      // "She bit into the apple."
    private String translation;   // "Cô ấy cắn vào quả táo."
}
```

---

## 3. API Endpoints

**Base path:** `/api/dictionary`  
**Auth:** Bearer token (tất cả endpoint đều yêu cầu login)

### 3.1 Thêm từ vào từ điển

```
POST /api/dictionary
```

**Request body** (`AddDictionaryWordRequest`):
```json
{
  "word": "apple",
  "ipa": "/ˈæp.əl/",
  "wordType": "noun",
  "meaning": "quả táo",
  "explanation": "A round fruit with red, green, or yellow skin.",
  "note": "Hay gặp trong IELTS Reading passage về healthy eating",
  "examples": [
    {
      "sentence": "She bit into the apple.",
      "translation": "Cô ấy cắn vào quả táo."
    }
  ],
  "isFavorite": false
}
```

**Validation:**
- `word`: required, max 200 chars
- `meaning`: required
- Nếu `word` đã tồn tại với `userId` → trả lỗi `409 Conflict`

**Response** `DefaultResponse<DictionaryWordResponse>`:
```json
{
  "success": true,
  "message": "OK",
  "data": { /* DictionaryWordResponse */ }
}
```

---

### 3.2 Lấy danh sách từ điển (có search & phân trang)

```
GET /api/dictionary?keyword=apple&wordType=noun&isFavorite=true&page=0&size=20&sortBy=createdAt&sortDir=desc
```

**Query params:**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `keyword` | String | — | Tìm kiếm theo `word` (LIKE, case-insensitive) |
| `wordType` | String | — | Lọc theo từ loại |
| `isFavorite` | Boolean | — | Lọc chỉ từ yêu thích |
| `page` | int | `0` | Trang hiện tại |
| `size` | int | `20` | Số phần tử mỗi trang |
| `sortBy` | String | `createdAt` | Sắp xếp theo: `createdAt`, `word` |
| `sortDir` | String | `desc` | `asc` / `desc` |

**Response** `DefaultResponse<PageResponse<DictionaryWordResponse>>`:
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "page": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3,
    "items": [ /* DictionaryWordResponse[] */ ]
  }
}
```

---

### 3.3 Lấy chi tiết 1 từ

```
GET /api/dictionary/{id}
```

**Response** `DefaultResponse<DictionaryWordResponse>`

---

### 3.4 Cập nhật từ

```
PUT /api/dictionary/{id}
```

**Request body** (`UpdateDictionaryWordRequest`): tương tự `AddDictionaryWordRequest`, tất cả field đều optional (PATCH-style logic trong service).

---

### 3.5 Xoá từ khỏi từ điển

```
DELETE /api/dictionary/{id}
```

**Response:**
```json
{ "success": true, "message": "Word removed from dictionary." }
```

---

### 3.6 Toggle yêu thích

```
PATCH /api/dictionary/{id}/favorite
```

Đảo ngược `isFavorite`. Trả về `DictionaryWordResponse` đã cập nhật.

---

## 4. DTOs

### `DictionaryWordResponse`
```json
{
  "id": "uuid",
  "word": "apple",
  "ipa": "/ˈæp.əl/",
  "wordType": "noun",
  "meaning": "quả táo",
  "explanation": "A round fruit with red, green, or yellow skin.",
  "note": "Hay gặp trong IELTS Reading...",
  "examples": [
    { "sentence": "...", "translation": "..." }
  ],
  "isFavorite": false,
  "createdAt": "2026-03-11T02:21:00Z",
  "updatedAt": "2026-03-11T02:21:00Z"
}
```

---

## 5. Cấu trúc Code

```
entity/
  UserDictionary.java              ← Entity mới

repository/
  UserDictionaryRepository.java    ← JpaRepository + custom @Query

service/
  DictionaryService.java           ← Interface
  impl/DictionaryServiceImpl.java  ← Implementation

controller/
  DictionaryController.java        ← /api/dictionary

dto/
  request/dictionary/
    AddDictionaryWordRequest.java
    UpdateDictionaryWordRequest.java
  response/dictionary/
    DictionaryWordResponse.java
    ExampleSentence.java
```

---

## 6. Repository — Query chính

```java
@Query("""
    SELECT d FROM UserDictionary d
    WHERE d.userId = :userId
      AND (:keyword IS NULL OR LOWER(d.word) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:wordType IS NULL OR d.wordType = :wordType)
      AND (:isFavorite IS NULL OR d.isFavorite = :isFavorite)
    """)
Page<UserDictionary> search(
    @Param("userId") UUID userId,
    @Param("keyword") String keyword,
    @Param("wordType") String wordType,
    @Param("isFavorite") Boolean isFavorite,
    Pageable pageable
);
```

---

## 7. Lưu ý Thiết Kế

> [!IMPORTANT]
> **Không dùng chung với `vocabulary_records`**: Bảng đó phục vụ review session (ghi lại đúng/sai khi luyện tập). Bảng `user_dictionary` là **cá nhân hóa**, user tự thêm vào để lưu giữ.

> [!TIP]
> Trong tương lai có thể mở rộng thêm:
> - **Gắn tag** (`tags JSONB`) để phân loại từ theo chủ đề (business, science, travel…)
> - **Liên kết với review session**: cho phép đưa từ trong dictionary vào quiz ôn tập
> - **Import/Export**: xuất CSV để học offline
> - **Nguồn gốc** (`source` field): từ được lấy từ đâu (IELTS test, speaking, tự thêm…)
