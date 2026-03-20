# Custom Speaking Conversation - FE Integration Guide

Tai lieu nay mo ta contract cho tinh nang `custom speaking conversation` (freestyle conversation).

## Muc tieu

User co the:
- Tao mot cuoc tro chuyen tieng Anh theo chu de tu do
- Chon option co dinh cho AI:
  - `style`
  - `personality`
  - `expertise`
- Bat/tat che do cham diem
- Trao doi voi AI qua REST hoac WebSocket
- Xem lich su va chi tiet conversation

## Base Paths

- REST base: `/api/custom-speaking-conversations`
- STOMP send: `/app/custom-speaking-conversation`
- STOMP subscribe: `/topic/custom-speaking-conversation/{userId}`
- STOMP endpoint handshake: `/ws/realtime-chat`

Ghi chu:
- WS dang dung chung STOMP endpoint hien co.
- FE can gui header `Authorization: Bearer <token>` khi connect STOMP.

## Enum Options

### style

```json
["CASUAL", "PROFESSIONAL", "ENCOURAGING", "CHALLENGING"]
```

### personality

```json
["FRIENDLY", "HUMOROUS", "PATIENT", "STRAIGHTFORWARD"]
```

### expertise

```json
["GENERAL", "BUSINESS", "TECHNOLOGY", "EDUCATION", "TRAVEL"]
```

## Conversation Status

Backend tra ve `status` theo cac gia tri sau:

```json
["IN_PROGRESS", "COMPLETED", "GRADING", "GRADED", "FAILED"]
```

Y nghia:
- `IN_PROGRESS`: dang hoi dap
- `COMPLETED`: user da ket thuc hoac da dat max turn
- `GRADING`: backend dang cham diem async
- `GRADED`: da co ket qua cham diem
- `FAILED`: cham diem loi

## Max Turn

- So turn toi da cua user duoc backend config qua env:
  - `CUSTOM_SPEAKING_CONVERSATION_MAX_USER_TURNS`
- Hien tai mac dinh la `100`
- FE khong can tu config hard-code, hay doc tu response:
  - `userTurnCount`
  - `maxUserTurns`

## REST APIs

### 1. Start conversation

`POST /api/custom-speaking-conversations/start`

Request:

```json
{
  "topic": "How technology changes the way people learn",
  "style": "PROFESSIONAL",
  "personality": "PATIENT",
  "expertise": "EDUCATION",
  "gradingEnabled": true
}
```

Response:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "conversationId": "3a76a53c-18a9-46e3-9b84-1554a1e6fd4f",
    "title": "Learning With Technology",
    "turnNumber": 1,
    "aiMessage": "Technology has changed learning in many interesting ways. From your point of view, what is the biggest change it has made for students?",
    "conversationComplete": false,
    "gradingEnabled": true,
    "status": "IN_PROGRESS",
    "userTurnCount": 0,
    "maxUserTurns": 100
  }
}
```

### 2. Submit one user turn

`POST /api/custom-speaking-conversations/{id}/turn`

Request:

```json
{
  "transcript": "I think the biggest change is that students can learn anytime, not only in classrooms.",
  "audioUrl": "https://cdn.example.com/audio/turn-1.mp3",
  "timeSpentSeconds": 22,
  "speechAnalytics": {
    "wordCount": 15,
    "wordsPerMinute": 112.5,
    "pauseCount": 2,
    "avgPauseDurationMs": 720,
    "longPauseCount": 0,
    "fillerWordCount": 1,
    "avgWordConfidence": 0.91,
    "fillerWords": ["um"],
    "lowConfidenceWords": ["classrooms"],
    "wordDetails": []
  }
}
```

Response khi conversation van tiep tuc:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "conversationId": "3a76a53c-18a9-46e3-9b84-1554a1e6fd4f",
    "title": "Learning With Technology",
    "turnNumber": 2,
    "aiMessage": "That makes sense, especially for people with busy schedules. Do you think online learning can fully replace face-to-face classes, or should they work together?",
    "conversationComplete": false,
    "gradingEnabled": true,
    "status": "IN_PROGRESS",
    "userTurnCount": 1,
    "maxUserTurns": 100
  }
}
```

Response khi conversation tu dong ket thuc vi dat max turn:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "conversationId": "3a76a53c-18a9-46e3-9b84-1554a1e6fd4f",
    "title": "Learning With Technology",
    "turnNumber": 100,
    "aiMessage": null,
    "conversationComplete": true,
    "gradingEnabled": true,
    "status": "COMPLETED",
    "userTurnCount": 100,
    "maxUserTurns": 100
  }
}
```

### 3. Finish conversation

`POST /api/custom-speaking-conversations/{id}/finish`

Request body: none

Response:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "conversationId": "3a76a53c-18a9-46e3-9b84-1554a1e6fd4f",
    "title": "Learning With Technology",
    "turnNumber": 6,
    "aiMessage": null,
    "conversationComplete": true,
    "gradingEnabled": true,
    "status": "COMPLETED",
    "userTurnCount": 5,
    "maxUserTurns": 100
  }
}
```

Luu y:
- Neu `gradingEnabled = true`, backend se cham diem async sau khi finish.
- FE nen goi API detail sau vai giay hoac refresh history/detail de nhan `GRADING` -> `GRADED`.

### 4. Get conversation detail

`GET /api/custom-speaking-conversations/{id}`

Response:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "3a76a53c-18a9-46e3-9b84-1554a1e6fd4f",
    "title": "Learning With Technology",
    "topic": "How technology changes the way people learn",
    "style": "PROFESSIONAL",
    "personality": "PATIENT",
    "expertise": "EDUCATION",
    "gradingEnabled": true,
    "status": "GRADED",
    "maxUserTurns": 100,
    "userTurnCount": 5,
    "totalTurns": 6,
    "timeSpentSeconds": 118,
    "fluencyScore": 7.0,
    "vocabularyScore": 6.5,
    "coherenceScore": 7.0,
    "pronunciationScore": 7.0,
    "overallScore": 7.0,
    "aiFeedback": "Markdown feedback from AI",
    "startedAt": "2026-03-20 15:21:10",
    "completedAt": "2026-03-20 15:24:30",
    "gradedAt": "2026-03-20 15:24:38",
    "turns": [
      {
        "id": "06e4f55a-55ef-4f6b-ac4f-d8adcd4b9f77",
        "turnNumber": 1,
        "aiMessage": "Technology has changed learning in many interesting ways. From your point of view, what is the biggest change it has made for students?",
        "userTranscript": "I think the biggest change is that students can learn anytime.",
        "audioUrl": "https://cdn.example.com/audio/turn-1.mp3",
        "timeSpentSeconds": 22,
        "speechAnalytics": {
          "wordCount": 11,
          "wordsPerMinute": 109.0,
          "pauseCount": 1,
          "avgPauseDurationMs": 640,
          "longPauseCount": 0,
          "fillerWordCount": 0,
          "avgWordConfidence": 0.92,
          "fillerWords": [],
          "lowConfidenceWords": [],
          "wordDetails": []
        },
        "createdAt": "2026-03-20 15:21:10"
      }
    ]
  }
}
```

### 5. Get conversation history

`GET /api/custom-speaking-conversations?page=0&size=10`

Response:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "items": [
      {
        "id": "3a76a53c-18a9-46e3-9b84-1554a1e6fd4f",
        "title": "Learning With Technology",
        "topic": "How technology changes the way people learn",
        "style": "PROFESSIONAL",
        "personality": "PATIENT",
        "expertise": "EDUCATION",
        "gradingEnabled": true,
        "status": "GRADED",
        "maxUserTurns": 100,
        "userTurnCount": 5,
        "totalTurns": 6,
        "timeSpentSeconds": 118,
        "fluencyScore": 7.0,
        "vocabularyScore": 6.5,
        "coherenceScore": 7.0,
        "pronunciationScore": 7.0,
        "overallScore": 7.0,
        "aiFeedback": "Markdown feedback from AI",
        "startedAt": "2026-03-20 15:21:10",
        "completedAt": "2026-03-20 15:24:30",
        "gradedAt": "2026-03-20 15:24:38",
        "turns": null
      }
    ]
  }
}
```

Luu y:
- O API history, `turns` se la `null`
- Muon render full transcript, goi API detail

## WebSocket Flow

## Subscribe

FE subscribe:

```text
/topic/custom-speaking-conversation/{userId}
```

## Send destination

FE send den:

```text
/app/custom-speaking-conversation
```

## WS payloads

### Action `start`

```json
{
  "action": "start",
  "topic": "How technology changes the way people learn",
  "style": "PROFESSIONAL",
  "personality": "PATIENT",
  "expertise": "EDUCATION",
  "gradingEnabled": true
}
```

### Action `submit`

```json
{
  "action": "submit",
  "conversationId": "3a76a53c-18a9-46e3-9b84-1554a1e6fd4f",
  "transcript": "I think the biggest change is that students can learn anytime, not only in classrooms.",
  "audioUrl": "https://cdn.example.com/audio/turn-1.mp3",
  "timeSpentSeconds": 22,
  "speechAnalytics": {
    "wordCount": 15,
    "wordsPerMinute": 112.5,
    "pauseCount": 2,
    "avgPauseDurationMs": 720,
    "longPauseCount": 0,
    "fillerWordCount": 1,
    "avgWordConfidence": 0.91,
    "fillerWords": ["um"],
    "lowConfidenceWords": ["classrooms"],
    "wordDetails": []
  }
}
```

### Action `finish`

```json
{
  "action": "finish",
  "conversationId": "3a76a53c-18a9-46e3-9b84-1554a1e6fd4f"
}
```

## WS response payload

### Normal AI message

```json
{
  "type": "AI_MESSAGE",
  "conversationId": "3a76a53c-18a9-46e3-9b84-1554a1e6fd4f",
  "title": "Learning With Technology",
  "turnNumber": 2,
  "aiMessage": "That makes sense, especially for people with busy schedules. Do you think online learning can fully replace face-to-face classes, or should they work together?",
  "audioBase64": "BASE64_MP3_OR_AUDIO_BYTES",
  "status": "IN_PROGRESS",
  "userTurnCount": 1,
  "maxUserTurns": 100,
  "errorMessage": null,
  "timestamp": "2026-03-20 15:22:01"
}
```

### Conversation complete

```json
{
  "type": "CONVERSATION_COMPLETE",
  "conversationId": "3a76a53c-18a9-46e3-9b84-1554a1e6fd4f",
  "title": "Learning With Technology",
  "turnNumber": 6,
  "aiMessage": null,
  "audioBase64": null,
  "status": "COMPLETED",
  "userTurnCount": 5,
  "maxUserTurns": 100,
  "errorMessage": null,
  "timestamp": "2026-03-20 15:24:30"
}
```

### Error

```json
{
  "type": "ERROR",
  "conversationId": null,
  "title": null,
  "turnNumber": null,
  "aiMessage": null,
  "audioBase64": null,
  "status": null,
  "userTurnCount": null,
  "maxUserTurns": null,
  "errorMessage": "Conversation not found: ...",
  "timestamp": "2026-03-20 15:24:30"
}
```

## FE Integration Recommendation

## Suggested UX flow

1. User chon `topic`, `style`, `personality`, `expertise`, `gradingEnabled`
2. FE goi REST `start` hoac gui WS action `start`
3. Hien thi `title` + `aiMessage` dau tien
4. User ghi am / STT / transcript
5. FE gui `submit`
6. Backend tra `AI_MESSAGE`, FE append vao chat UI
7. Lap lai den khi user bam `finish` hoac backend tra `conversationComplete = true`
8. Neu `gradingEnabled = true`, FE poll detail/history de cap nhat `GRADING -> GRADED`

## UI state can render

- `IN_PROGRESS`: chat input duoc phep gui
- `COMPLETED`: khoa input, cho feedback neu co cham diem
- `GRADING`: hien loading "Dang cham diem..."
- `GRADED`: hien score + markdown feedback
- `FAILED`: hien thong bao loi cham diem, van cho xem transcript

## Important notes

- `title` duoc AI sinh khi start conversation
- `aiMessage` co the `null` khi conversation complete
- `audioBase64` chi co tren WS response, khong co trong REST response
- `gradingEnabled` duoc snapshot luc start, FE khong doi trong giua conversation
- Neu FE da co STT analytics, gui kem `speechAnalytics`; neu khong co thi bo qua
- Backend hien khong push event rieng khi grading xong, FE nen poll detail/history

## Error handling

FE nen handle:
- REST error theo response wrapper hoac HTTP error
- WS event `type = ERROR`
- Conversation co the khong con `IN_PROGRESS` neu user da finish o tab khac

## Backend files tham khao

- `src/main/java/com/swpts/enpracticebe/controller/CustomSpeakingConversationController.java`
- `src/main/java/com/swpts/enpracticebe/controller/CustomSpeakingWebSocketController.java`
- `src/main/java/com/swpts/enpracticebe/dto/request/speaking/StartCustomConversationRequest.java`
- `src/main/java/com/swpts/enpracticebe/dto/request/speaking/SubmitCustomConversationTurnRequest.java`
- `src/main/java/com/swpts/enpracticebe/dto/request/speaking/CustomSpeakingConversationMessage.java`
- `src/main/java/com/swpts/enpracticebe/dto/response/speaking/CustomConversationResponse.java`
- `src/main/java/com/swpts/enpracticebe/dto/response/speaking/CustomConversationStepResponse.java`
- `src/main/java/com/swpts/enpracticebe/dto/response/speaking/CustomSpeakingConversationWsResponse.java`
