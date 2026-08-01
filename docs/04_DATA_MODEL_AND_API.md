# 📊 04. Data Model & API Specification (데이터 모델 및 API 명세서)

> **⚠️ 구현 현황 — 「단일 기기 데모 / 백엔드 미구현」 단계 (2026-08-01 기준)**
> **1장의 데이터 모델(도메인 엔티티)은 `:core:model`에 실제로 구현돼 있습니다.** 반면 **3장의 WebSocket/Firebase 실시간 프로토콜(`SEND_LOCATION`, `LOCATION_UPDATED`, `GROUP_STATUS_CHANGED`, `USER_ARRIVED`)과 2장의 서버 측 딥링크 처리는 아직 구현되지 않았습니다.** 현재 상태 전이·좌표 전달은 인메모리 `FakeMeetPinRepository` 내부에서만 이뤄지는 단일 기기 데모입니다.

## 1. 데이터 모델 명세 (Data Models - Kotlin / JSON)

### 1.1 `MeetPinGroup` (약속 모임 정보)
```kotlin
enum class GroupStatus {
    LOBBY,      // 초대 진행 중 & 승낙 대기 중
    ACTIVE,     // 전원 승낙 완료 & 실시간 위치 공유 중
    FINISHED,   // 모임 완료 (위치 공유 종료)
    CANCELLED   // 취소됨
}

data class MeetPinGroup(
    val id: String,                  // 약속 고유 ID (UUID or Short ID)
    val title: String,               // 약속 이름 (예: "강남역 모임")
    val hostId: String,              // 약속 주선자 사용자 ID
    val pinLocation: PinLocation,    // 약속 장소 좌표 및 장소 정보
    val scheduledAt: Long,           // 약속 일시 (Epoch Timestamp in millis)
    val status: GroupStatus,         // 현재 모임 상태
    val inviteCode: String,          // 딥링크용 고유 초대 코드
    val createdAt: Long              // 생성 일시
)
```

### 1.2 `PinLocation` (약속 장소 핀)
```kotlin
data class PinLocation(
    val placeName: String,           // 장소명 (예: "강남역 11번 출구")
    val address: String?,            // 상세 주소
    val latitude: Double,            // 위도 (Latitude)
    val longitude: Double            // 경도 (Longitude)
)
```

### 1.3 `Participant` (참가자 및 수락 상태)
```kotlin
enum class InviteStatus {
    PENDING,    // 초대 대기 중
    ACCEPTED,   // 승낙함
    DECLINED    // 거절함
}

data class Participant(
    val userId: String,              // 사용자 고유 ID
    val nickname: String,            // 표시 이름
    val profileImageUrl: String?,    // 프로필 이미지 URL
    val inviteStatus: InviteStatus,  // 승낙 상태
    val isArrived: Boolean = false,  // 약속 장소 도착 여부
    val arrivedAt: Long? = null      // 도착 시각
)
```

### 1.4 `LocationUpdate` (실시간 좌표 패킷)
```kotlin
data class LocationUpdate(
    val groupId: String,             // 속한 약속 ID
    val userId: String,              // 사용자 ID
    val latitude: Double,            // 현재 위도
    val longitude: Double,           // 현재 경도
    val bearing: Float? = null,      // 이동 방향 (도 단위 0~360)
    val speed: Float? = null,        // 이동 속도 (m/s)
    val accuracy: Float? = null,     // 위치 오차 범위 (미터)
    val timestamp: Long              // 수집 시각
)
```

---

## 2. 딥링크 규격 (Deep Link Specification)

- **Scheme Standard**: `https://meetpin.app/invite/{inviteCode}` 또는 App Link `meetpin://invite?code={inviteCode}`
- **파라미터 전달**:
  - `inviteCode`: 고유 약속 초대 키
- **처리 흐름**:
  1. 앱 설치 유무 확인 (미설치 시 Google Play Store로 리다이렉트).
  2. 앱 실행 후 `LobbyActivity/LobbyScreen`으로 인텐트 전송.
  3. `inviteCode`로 약속 단방향 조회를 실행하여 약속 정보 및 수락 팝업 노출.

---

## 3. 실시간 프로토콜 메시지 명세 (WebSocket / Firebase Realtime Event)

### 3.1 위치 전송 이벤트 (Client ➔ Server)
**Event Name**: `SEND_LOCATION`
```json
{
  "event": "SEND_LOCATION",
  "groupId": "group_abc123",
  "userId": "user_789",
  "latitude": 37.497942,
  "longitude": 127.027621,
  "bearing": 180.5,
  "speed": 1.2,
  "accuracy": 5.0,
  "timestamp": 1722000000000
}
```

### 3.2 위치 브로드캐스트 이벤트 (Server ➔ Clients)
**Event Name**: `LOCATION_UPDATED`
```json
{
  "event": "LOCATION_UPDATED",
  "groupId": "group_abc123",
  "userId": "user_789",
  "latitude": 37.497942,
  "longitude": 127.027621,
  "bearing": 180.5,
  "speed": 1.2,
  "timestamp": 1722000000000
}
```

### 3.3 상태 변경 이벤트 (Server ➔ Clients)
**Event Name**: `GROUP_STATUS_CHANGED`
- 모든 참가자 승낙 시 `LOBBY` ➔ `ACTIVE` 전환 알림.
```json
{
  "event": "GROUP_STATUS_CHANGED",
  "groupId": "group_abc123",
  "newStatus": "ACTIVE",
  "message": "모든 인원이 승낙했습니다. 실시간 위치 공유가 시작됩니다."
}
```

### 3.4 도착 감지 이벤트 (Client ➔ Server & Server ➔ Clients)
**Event Name**: `USER_ARRIVED`
```json
{
  "event": "USER_ARRIVED",
  "groupId": "group_abc123",
  "userId": "user_789",
  "arrivedAt": 1722000500000
}
```
