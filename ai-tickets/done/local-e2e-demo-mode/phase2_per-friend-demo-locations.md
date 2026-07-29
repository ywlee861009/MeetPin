# phase2: 친구별 고정 데모 위치(광화문/강남) 소스 배관

## 문제 / 목표
WIP(`bc73438`)는 호스트 외 모든 참가자를 단일 상수 `FRIEND_TEST_LOCATION`(광화문)으로
찍어 친구가 겹친다. 친구별로 **서로 다른 고정 좌표**를 부여해 지도에 3개 마커가
각자 위치(나=실제 GPS, 친구1=광화문, 친구2=강남)에 뜨도록 한다.

## 좌표 (안)
- 광화문: `GeoPoint(37.5759, 126.9769)`
- 강남역: `GeoPoint(37.4979, 127.0276)`
- 목적지 핀(기본): 시청 부근 `GeoPoint(37.5666805, 126.9784147)` — 기존 default 유지

## 구현 방향 — 안 A (권장: observeGroupLocations 복원)
1. **데모 위치 소스**: `core/location`의 `DefaultLocationRepository.updateGroupLocations(...)`
   가 이미 있으므로, 데모 시작 시 친구별 `LocationUpdate` 리스트를 이 스트림에 방출한다.
   - 방출 주체는 (a) 별도 `DemoScenarioController`(권장, `@Singleton`, Hilt 주입) 또는
     (b) 트래킹 진입 시 ViewModel이 1회 seed. 데모 로직 격리를 위해 (a)를 권장.
   - `LocationUpdate(groupId, userId=FRIEND1_USER_ID, lat=광화문...)`, friend2도 동일.
2. **ViewModel 복원**: `LiveTrackingViewModel.startTracking`의 combine을 3-way로 되돌린다:
   ```
   combine(observeGroup, getCurrentLocation(호스트), observeGroupLocations(친구들))
   ```
   - position 결정: `isHost -> myLocation ?: pinLatLng`
     `else -> groupLocations.find { it.userId == participant.userId } 좌표 ?: pinLatLng`
   - WIP가 추가한 단일 `FRIEND_TEST_LOCATION` 분기 제거.
   - `currentHostId` 캐시는 유지(채팅/합류 식별에 사용).
3. WIP가 없앤 `observeGroupLocations`·`arrivalDetector.calculateDistance(위치, 핀)` 흐름이
   자연스럽게 부활한다.

## 구현 방향 — 안 B (빠름: ViewModel 데모 테이블)
- ViewModel companion에 `val DEMO_FRIEND_LOCATIONS = mapOf(FRIEND1_USER_ID to 광화문, FRIEND2_USER_ID to 강남)`
  를 두고 `else -> DEMO_FRIEND_LOCATIONS[participant.userId] ?: pinLatLng`.
- 배관 없이 즉시 동작하나 데모 좌표가 프로덕션 코드에 남는다. movement(phase4) 확장 시 재작업.

## 검증
- [x] `:feature:tracking` 컴파일, `:app` 컴파일 (EXIT=0) — 안 B라 `:core:location` 미변경
- [ ] 트래킹 지도에 마커 3개가 **서로 다른 위치**(내 GPS/광화문/강남)에 표시 — (런타임, phase3 이후 E2E 패스)
- [ ] 하단 시트에 친구2명의 거리/ETA가 좌표에 맞게 다르게 계산되어 표시 — (런타임, phase3 이후 E2E 패스)
- [x] `DesignSystemGuardrailTest` 영향 없음 — phase2는 UI 변경 없음(ViewModel 로직만)

## 구현 메모 (안 B · index 기반)
- `observeGroupLocations`/`updateGroupLocations`/3-way combine 배관 **불필요** — 기존 2-way combine 유지.
- `startTracking`에서 ACCEPTED 참가자 중 비호스트에 순번을 매겨(`friendIndexByUserId`)
  `demoFriendLocation(index)`로 좌표 배정. 좌표 수 초과 시 modulo 순환.
- 기존 `FRIEND_TEST_LOCATION`(단일 광화문) 제거 → `DEMO_FRIEND_LOCATIONS`(광화문/강남 리스트).
