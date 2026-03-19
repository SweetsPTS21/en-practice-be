# Realtime Chat Streaming Spec

Tai lieu nay mo ta contract WebSocket/STOMP cho luong `/realtime-chat` sau khi backend da ho tro stream tu OpenClaw voi `stream: true`.

## Tong quan

Frontend se:
- Ket noi STOMP WebSocket toi endpoint `/ws/realtime-chat`
- Subscribe vao topic rieng cua user: `/topic/realtime-chat/{userId}`
- Gui prompt toi app destination `/app/realtime-chat`
- Nhan response theo nhieu event nho thay vi 1 message day du

Backend se stream theo kieu ChatGPT/OpenAI:
- `START`: bat dau 1 AI message moi
- `DELTA`: them text tung doan
- `COMPLETE`: danh dau stream ket thuc thanh cong
- `ERROR`: stream loi va dung

## Ket noi WebSocket

### Endpoint

`/ws/realtime-chat`

### Auth

Gui JWT trong STOMP `CONNECT` header:

```text
Authorization: Bearer <access_token>
```

Neu token hop le, backend se map session voi `userId` va frontend se nhan duoc message tu topic rieng cua user.

## Subscribe

Frontend subscribe vao:

```text
/topic/realtime-chat/{userId}
```

Vi du:

```text
/topic/realtime-chat/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

## Gui message len backend

### Destination

```text
/app/realtime-chat
```

### Payload

```json
{
  "content": "Hello, can you help me practice English?",
  "senderId": "user-id-or-client-id",
  "timestamp": "2026-03-19T18:00:00Z"
}
```

### Luu y

- Backend hien tai chi dung truong `content`.
- `senderId` va `timestamp` co the giu lai de frontend tu quan ly state, nhung backend khong phu thuoc vao 2 truong nay.

## Payload backend tra ve

Backend se gui object co schema sau:

```json
{
  "requestId": "c0b5b7fb-c2d7-4c23-9b3d-1d8b7f6ce111",
  "messageId": "7a0d4db9-0cc3-4aa9-b2db-4e18d1f50b22",
  "type": "DELTA",
  "content": "Hello",
  "senderId": "AI",
  "final": false,
  "timestamp": "2026-03-19 18:00:01"
}
```

## Y nghia cac truong

- `requestId`: dinh danh cho 1 lan user gui prompt.
- `messageId`: dinh danh cho AI message dang duoc render.
- `type`: `START`, `DELTA`, `COMPLETE`, `ERROR`.
- `content`: noi dung text cua event.
- `senderId`: hien tai backend gui gia tri AI constant.
- `final`: `true` khi `COMPLETE` hoac `ERROR`.
- `timestamp`: thoi diem backend tao event.

## Luong event

### 1. START

Backend gui ngay khi bat dau stream.

```json
{
  "requestId": "req-1",
  "messageId": "msg-1",
  "type": "START",
  "content": "",
  "senderId": "AI",
  "final": false,
  "timestamp": "2026-03-19 18:00:01"
}
```

Frontend nen:
- Tao 1 bubble AI rong
- Luu `requestId`, `messageId`
- Bat trang thai loading/typing cho bubble do

### 2. DELTA

Backend gui nhieu lan, moi lan la 1 doan text moi can append.

```json
{
  "requestId": "req-1",
  "messageId": "msg-1",
  "type": "DELTA",
  "content": "Hello",
  "senderId": "AI",
  "final": false,
  "timestamp": "2026-03-19 18:00:01"
}
```

```json
{
  "requestId": "req-1",
  "messageId": "msg-1",
  "type": "DELTA",
  "content": ", how can I help you practice today?",
  "senderId": "AI",
  "final": false,
  "timestamp": "2026-03-19 18:00:02"
}
```

Frontend nen:
- Tim bubble theo `messageId`
- Append `content` vao noi dung hien tai
- Khong overwrite toan bo message neu dang dung kieu append

### 3. COMPLETE

Backend gui 1 lan khi stream ket thuc thanh cong.

```json
{
  "requestId": "req-1",
  "messageId": "msg-1",
  "type": "COMPLETE",
  "content": "Hello, how can I help you practice today?",
  "senderId": "AI",
  "final": true,
  "timestamp": "2026-03-19 18:00:03"
}
```

Frontend nen:
- Tat loading/typing
- Danh dau message da hoan tat
- Co the dong bo noi dung cuoi cung bang `content` neu muon chuan hoa state

### 4. ERROR

Backend gui khi stream loi.

```json
{
  "requestId": "req-1",
  "messageId": "msg-1",
  "type": "ERROR",
  "content": "Xin loi nha, hien tai toi khong the tra loi tin nhan cua ban",
  "senderId": "AI",
  "final": true,
  "timestamp": "2026-03-19 18:00:03"
}
```

Frontend nen:
- Tat loading/typing
- Hien thi loi trong bubble hien tai hoac toast
- Danh dau request da ket thuc

## Xu ly state de xuat

Moi AI message nen co state local nhu sau:

```ts
type StreamingAiMessage = {
  requestId: string;
  messageId: string;
  senderId: string;
  content: string;
  status: 'streaming' | 'completed' | 'error';
  timestamp: string;
};
```

## Pseudocode frontend

```ts
stompClient.subscribe(`/topic/realtime-chat/${userId}`, frame => {
  const event = JSON.parse(frame.body);

  switch (event.type) {
    case 'START': {
      upsertMessage({
        requestId: event.requestId,
        messageId: event.messageId,
        senderId: event.senderId,
        content: '',
        status: 'streaming',
        timestamp: event.timestamp,
      });
      break;
    }

    case 'DELTA': {
      appendMessageContent(event.messageId, event.content);
      break;
    }

    case 'COMPLETE': {
      finalizeMessage(event.messageId, event.content);
      break;
    }

    case 'ERROR': {
      markMessageError(event.messageId, event.content);
      break;
    }
  }
});
```

## Hanh vi can luu y

### 1. User gui prompt moi khi prompt cu dang stream

Backend hien tai se huy stream cu theo `userId` truoc khi tao stream moi.

Frontend nen:
- Cho phep bubble cu dung o trang thai dang do
- Hoac danh dau bubble cu la interrupted neu can UX ro hon
- Khong append nham chunk cua request moi vao bubble cu, hay match theo `messageId`

### 2. Khong duoc gom DELTA bang cach replace text

`DELTA.content` la phan text moi, khong dam bao la toan bo noi dung tich luy.

Can:
- append cho moi `DELTA`
- dung `COMPLETE.content` neu muon reconcile lai state cuoi cung

### 3. Hien typing indicator

Goi y:
- Bat indicator khi nhan `START`
- Giu indicator trong suot cac `DELTA`
- Tat indicator khi nhan `COMPLETE` hoac `ERROR`

### 4. Retry

Neu WebSocket/STOMP mat ket noi:
- reconnect client
- resubscribe topic `/topic/realtime-chat/{userId}`
- khong ky vong backend tiep tuc stream cu sau reconnect

## Vi du sequence day du

### User gui

```json
{
  "content": "Give me a short English greeting",
  "senderId": "user-123",
  "timestamp": "2026-03-19T18:00:00Z"
}
```

### Backend tra ve

```json
{
  "requestId": "req-1",
  "messageId": "msg-1",
  "type": "START",
  "content": "",
  "senderId": "AI",
  "final": false,
  "timestamp": "2026-03-19 18:00:01"
}
```

```json
{
  "requestId": "req-1",
  "messageId": "msg-1",
  "type": "DELTA",
  "content": "Hello",
  "senderId": "AI",
  "final": false,
  "timestamp": "2026-03-19 18:00:01"
}
```

```json
{
  "requestId": "req-1",
  "messageId": "msg-1",
  "type": "DELTA",
  "content": "! Nice to meet you.",
  "senderId": "AI",
  "final": false,
  "timestamp": "2026-03-19 18:00:02"
}
```

```json
{
  "requestId": "req-1",
  "messageId": "msg-1",
  "type": "COMPLETE",
  "content": "Hello! Nice to meet you.",
  "senderId": "AI",
  "final": true,
  "timestamp": "2026-03-19 18:00:02"
}
```

## Checklist cho frontend

- Ket noi STOMP voi header `Authorization: Bearer <token>`
- Subscribe dung topic `/topic/realtime-chat/{userId}`
- Gui message toi `/app/realtime-chat`
- Tao bubble khi nhan `START`
- Append text khi nhan `DELTA`
- Ket thuc bubble khi nhan `COMPLETE`
- Hien thi fallback khi nhan `ERROR`
- Match message bang `messageId`, khong chi dua vao `senderId`

## Ghi chu tuong thich

Backend van con cac API sync khac cho nhung luong khong can realtime. Tai lieu nay chi ap dung cho WebSocket `/realtime-chat`.
