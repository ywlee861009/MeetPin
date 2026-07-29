# local-e2e-demo-mode — 단일 기기 백엔드리스 E2E 데모 모드

## 🎯 목적
서버 없이 **기기 1대**에서 라이브 트래킹의 전체 UX를 눈으로 확인한다.
가상의 참가자 3명을 띄운다:

| 역할 | 닉네임(안) | 위치 | 채팅 |
|---|---|---|---|
| 나(호스트) | 나 (호스트) | **실제 GPS** (없으면 핀으로 폴백) | 채팅 입력창으로 전송 |
| 친구1 | 광화문 친구 | **광화문** (37.5759, 126.9769) | 더미 버튼으로 트리거 |
| 친구2 | 강남 친구 | **강남역** (37.4979, 127.0276) | 더미 버튼으로 트리거 |

지도에 3개 마커가 서로 다른 위치에 뜨고, 친구별 말풍선이 나타났다 사라지며,
하단 참가자 시트에 거리/ETA가 3줄로 보이는 것 — 이 UI를 백엔드 없이 재현하는 것이 목표.

## 📖 배경 (현재 코드 기준)
- `core/data/.../FakeMeetPinRepository.createGroup`은 **호스트 + 친구 1명(PENDING)** 만 시드한다.
  (`HOST_USER_ID` / `GUEST_USER_ID` 두 상수뿐)
- `LiveTrackingViewModel.startTracking`은 최근 WIP 커밋(`bc73438`)에서
  `observeGroupLocations`를 **우회**하고 호스트=실제 GPS, 그 외 전원=단일 상수
  `FRIEND_TEST_LOCATION`(광화문)으로 찍고 있다 → 친구가 여럿이어도 한 점에 겹친다.
- `core/location/.../DefaultLocationRepository.observeGroupLocations`는 `_groupLocations`
  SharedFlow를 반환하지만, 이를 먹여주는 `updateGroupLocations`를 **아무도 호출하지 않는다**
  → 친구 위치의 실질적 소스가 없다. (WIP가 광화문 상수로 때운 이유)
- `simulateGuestChat`(디버그)은 **첫 번째 친구**에게 **고정 문구** 하나만 띄우고,
  `sendChat`과 달리 **자동으로 닫히지 않는다**.
- 트래킹 화면 진입은 핀 생성 → 대기실 → 전원 승낙(디버그 `SimulateGuestAccept`) → 트래킹.
  이 경로는 이미 단일 기기에서 동작한다.

## 🧩 핵심 설계 결정 (착수 전 확정 필요)
**친구 위치/움직임/채팅의 소스를 어디에 둘 것인가** — 두 안이 있다.

- **[안 A · 권장] 데모 위치 소스로 `observeGroupLocations`를 되살린다.**
  데모용 컴포넌트(예: `DefaultLocationRepository.updateGroupLocations` 활용 or 별도
  `DemoScenarioController`)가 친구별 `LocationUpdate`를 방출하고, ViewModel은
  `observeGroupLocations`를 다시 구독한다(호스트만 실제 GPS로 덮어씀).
  - 장점: 데모 데이터가 feature ViewModel 밖(data/location 계층)에 격리된다.
    움직임·향후 실제 WebSocket 연동과 구조가 같다. WIP가 없앤 `observeGroupLocations`가 부활한다.
  - 단점: 3개 Flow(`observeGroup` + `getCurrentLocation` + `observeGroupLocations`) combine 배관 필요.
- **[안 B · 빠름] ViewModel에 `Map<userId, GeoPoint>` 데모 테이블을 둔다.**
  - 장점: 변경 최소, 즉시 보임.
  - 단점: 데모 좌표가 프로덕션 ViewModel에 하드코딩됨. 움직임/네트워크 확장 시 다시 뜯음.

phase 문서는 **안 A** 기준으로 작성한다. 안 B로 갈 경우 phase2를 그에 맞게 축소한다.

> **결정(2026-07-29): 안 B 채택 + index 기반 변형.**
> `Map<userId, GeoPoint>` 대신, 트래킹 화면이 **비호스트 참가자 순번(index)** 으로
> 좌표를 배정한다(첫 친구→광화문, 둘째→강남). 이유: 안 B 원안대로 userId 키를 쓰면
> data 모듈의 private 상수를 feature 모듈에 노출/중복해야 하는 커플링이 생긴다.
> index 기반이면 데모 좌표 테이블이 ViewModel 안에서 자기완결된다.
> → phase2는 `observeGroupLocations`/`updateGroupLocations`/3-way combine 배관 없이
>   기존 combine의 `else -> FRIEND_TEST_LOCATION` 분기만 순번 조회로 교체한다.
> → phase3의 채팅 시뮬 Intent도 userId 대신 friend 순번(index) 기반으로 둔다.

## 📋 하위 티켓
| Phase | 파일 | 요약 | 필수 |
|---|---|---|---|
| 1 | `phase1_seed-three-participants.md` | Fake 시드를 호스트+친구2명으로 확장 | 필수 (✅ 코드/컴파일 완료, 런타임 검증 phase3 후) |
| 2 | `phase2_per-friend-demo-locations.md` | 친구별 고정 위치(광화문/강남) 소스 배관, ViewModel 복원 | 필수 (✅ 코드/컴파일 완료, 런타임 검증 phase3 후) |
| 3 | `phase3_chat-simulation.md` | 친구별 채팅 더미 버튼 + 캔드 메시지 + 자동 닫힘 | 필수 (✅ 코드/컴파일/가드레일 완료, 런타임 검증 대기) |
| 4 | `phase4_movement-and-arrival.md` | (선택) 친구가 목적지로 이동하는 애니메이션 + 도착 상태 | 선택 (todo 유지) |
| 5 | `phase5_quick-demo-entry.md` | (선택) 핀 생성 건너뛰고 데모 그룹으로 바로 진입하는 디버그 버튼/딥링크 | 선택 (todo 유지) |

> **참고**: 도착 상태(`isArrived`) 반영 수단(`MeetPinRepository.reportArrival`)은
> `post-navigation-gaps`의 phase4에서 이미 구현됨. phase4(친구 이동→도착) 착수 시 재사용한다.

## 🔗 선행 관계
- phase1 → phase2 → phase3 (핵심 데모 완성). phase4·5는 독립적 선택 사항.
- **외부 의존**: 지도 타일이 회색이면 데모의 시각적 효과가 반감된다.
  `post-navigation-gaps`의 phase1(`MAPS_API_KEY`) 해결이 병행되면 좋다.
  (마커·말풍선·시트는 키 없이도 회색 지도 위에 렌더된다.)

## ⚠️ 주의
- 모든 데모/디버그 UI는 기존 `showDebugTools` 플래그로 게이팅한다(프로덕션 노출 금지).
- 새 UI 버튼은 **DS 컴포넌트**(`MeetPinSecondaryButton` 등)만 사용한다.
  M3 원자를 직접 쓰면 `DesignSystemGuardrailTest`가 깨진다.
- 이 티켓은 WIP 커밋(`bc73438`)의 임시 친구-광화문 로직을 **정식 데모 소스로 대체**한다.

## 상태
- ✅ 핵심(phase1~3) 완료 및 런타임 검증 완료 → 에픽 종료 (2026-07-30)
- 선택 phase4·5는 `todo`에 남겨둠 (필요 시 착수)
