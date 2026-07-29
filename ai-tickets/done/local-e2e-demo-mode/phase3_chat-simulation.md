# phase3: 친구별 채팅 시뮬레이션 (더미 버튼 + 자동 닫힘)

## 문제 / 목표
현재 `simulateGuestChat`은 **첫 번째 친구**에게 **고정 문구** 하나만 띄우고
자동으로 사라지지 않는다. 친구2명 각각에게, 여러 캔드 메시지를, 자동 닫힘과 함께
띄울 수 있게 한다. 목표는 "친구1/친구2 말풍선이 번갈아 뜨고 몇 초 뒤 사라지는" 모습 확인.

## 수정 대상
- `android/feature/tracking/.../LiveTrackingContract.kt` — Intent 확장
- `android/feature/tracking/.../LiveTrackingViewModel.kt` — 채팅 시뮬 로직
- `android/feature/tracking/.../LiveTrackingScreen.kt` — 디버그 버튼 (DS 컴포넌트)

## 구현 방향
1. **Intent**: `SimulateGuestChat`을 대상 지정형으로 바꾼다.
   ```kotlin
   data class SimulateGuestChat(val userId: String) : LiveTrackingIntent
   ```
   (또는 `data object`를 유지하되 "다음 친구/랜덤 친구"로 순환. 대상 지정이 데모 통제에 유리.)
2. **ViewModel**:
   - 대상 friend의 마커에 캔드 메시지를 설정(`chatMessage`, `chatTimestamp`).
   - `sendChat`에 이미 있는 **자동 닫힘 패턴**(`delay(CHAT_BUBBLE_DURATION_MS)` 후 null)을
     공통 헬퍼로 추출해 시뮬 채팅에도 적용한다. 현재 `simulateGuestChat`은 이 처리가 없다.
   - 캔드 메시지 예: `listOf("거의 다 왔어! 🏃", "5분 뒤 도착~", "커피 사갈까? ☕", "먼저 자리 잡을게")`.
     호출마다 순환(인덱스는 마커별로 관리하거나 timestamp 기반으로 단순 회전).
   - `sendChat`의 "index==0을 나로 가정" 하드코딩도 `currentHostId` 기반으로 정리하면 좋다(선택).
3. **UI (showDebugTools 게이팅)**: 기존 "🧪 상대 채팅" 버튼을 친구별로 확장.
   - 예: "🧪 광화문 친구 채팅", "🧪 강남 친구 채팅" 2개 `MeetPinSecondaryButton`.
   - 버튼이 늘어나면 한 Row가 좁아지므로 2행 배치 또는 `FlowRow` 고려.

## 주의
- 말풍선은 지도 마커 위(`AnimatedParticipantMarker`)와 화면 밖 오버레이(offscreen) 두 곳에
  렌더된다. 둘 다 `MeetPinChatBubble`을 쓰므로 상태(`chatMessage`)만 바꾸면 양쪽 반영된다.
- 새 버튼은 반드시 DS 컴포넌트 사용(가드레일).

## 검증
- [x] `:feature:tracking`, `:app` 컴파일 (EXIT=0), `DesignSystemGuardrailTest` 그린 (EXIT=0)
- [ ] 광화문/강남 친구 버튼을 각각 눌러 **해당 마커**에 말풍선 표시 확인 — (런타임, E2E 패스)
- [ ] 말풍선이 `CHAT_BUBBLE_DURATION_MS`(4초) 후 자동으로 사라짐 — (런타임, E2E 패스)
- [ ] 친구가 화면 밖일 때 offscreen 말풍선으로도 표시됨 — (런타임, E2E 패스)

## 구현 메모 (index 기반)
- Intent: `data object SimulateGuestChat` → `data class SimulateGuestChat(val friendIndex: Int)`.
  UI가 userId를 몰라도 순번(0=광화문, 1=강남)으로 친구 지정.
- `sendChat`/`simulateGuestChat`의 말풍선 표시+자동닫힘을 `showChatBubble(userId, message)`로 공통화.
  timestamp 가드로 그 사이 새 말풍선이 뜨면 이전 코루틴이 지우지 않음(친구별 독립 타이머).
- `sendChat`의 "index==0을 나로 가정" 하드코딩 제거 → `currentHostId` 기반(폴백: 첫 마커).
- `FRIEND_TEST_CHAT`(단일 문구) 제거 → `CANNED_GUEST_CHATS`(4개) 회전.
- UI: "🧪 상대 채팅" 1개 → "🧪 광화문 친구"/"🧪 강남 친구" 2개. 버튼 3개가 되어 Column+Row 2행 배치
  (FlowRow 실험 API 회피). 모두 `MeetPinSecondaryButton`(DS) 사용 → 가드레일 그린.
