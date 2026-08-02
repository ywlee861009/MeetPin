# 출발 알림 (ETA 기반) (eta-departure-alert)

## 🎯 목적과 배경
모임 시간에 늦지 않도록 각 사용자의 현재 위치에서 목적지까지의 이동 시간을 계산하여, 적절한 시간에 "지금 출발해야 늦지 않아요!"라는 푸시 알림을 제공합니다.

## 📝 하위 티켓 목록
- `phase1_eta-calculation-logic.md`: 목적지까지의 도보/대중교통 소요 시간(ETA) 계산 로직 연동
- `phase2_departure-time-worker.md`: 백그라운드에서 주기적으로 출발 여부를 체크하는 WorkManager 구성
- `phase3_push-notification.md`: 출발 임박 시 로컬 푸시 알림 트리거 및 UI 연동

## 🔄 선행 관계
- phase1 ➔ phase2 ➔ phase3 순으로 진행

---

## ✅ 완료 (2026-08-02)

3개 phase 모두 구현 + **빌드·단위테스트 + 실기기 e2e 검증 완료.**

| Phase | 상태 | 결과물 |
|---|---|---|
| 1 | ✅ 완료 | `TravelMode`(도보/대중교통) + `CalculateEtaUseCase` 이동수단 오버로드 + `ShouldDepartNowUseCase`, 테스트 11종 |
| 2 | ✅ 완료 | `DepartureAlertScheduler`(주기 15분 unique work + `checkNow` 즉시 1회) + `DepartureCheckWorker`(위치→거리→ETA→판정→알림 후 자기 취소) + `LiveTrackingViewModel` 트리거 |
| 3 | ✅ 완료 | `DepartureNotifier`("출발 알림" 채널·HIGH) + `meetpin://track/{groupId}` 딥링크(manifest·`DeepLinkHandler`·`MainActivity`·`MeetPinNavHost`) |

### 실기기 e2e 검증 (Medium_Phone_API_35 / Android 15)
데모 그룹은 `scheduledAt = now`라 위치만 잡히면 출발 조건이 성립한다. 에뮬레이터 위치를 강남역
(핀=서울시청과 8.8km 이격)으로 두고 확인:
- **주기 워커 발화 → 알림 게시**: dumpsys 확정 — 채널 `meetpin_departure_channel`(HIGH),
  title="여기서 만나요", text="약 44분 · 지금 출발하세요", AUTO_CANCEL / category=reminder.
- **ETA 이원화 확인**: 지도 라벨 "약 1시간 45분"(도보 5km/h) vs 알림 "44분"(대중교통 오버로드) — 각 모델대로 계산.
- **디버그 버튼**: 트래킹 화면 "🧪 출발 알림 테스트"(`showDebugTools`) → `DepartureAlertScheduler.checkNow`
  (OneTimeWork 즉시 실행) → 알림 재발송 + Toast. 주기 15분을 기다리지 않고 전 경로를 확인하는 e2e 트리거.
- **딥링크**: 알림 탭 → `meetpin://track/group-1` → LiveTrackingScreen 진입.

### 티켓 명세와 달라진 설계 결정 (근거)
1. **신규 `EtaCalculator.kt` 대신 기존 `CalculateEtaUseCase`(core:domain) 고도화** — 동일 책임의
   use case가 이미 있고 그 주석이 이 티켓을 지목한다. 새 파일은 ETA 로직을 이원화하므로 지양.
   기존 `invoke(distance, speed)` 동작·테스트는 보존하고 이동수단 오버로드를 가산 추가.
2. **`@HiltWorker` 대신 순수 `CoroutineWorker`** — 기존 `LocationTrackingService`처럼 의존성을
   직접 생성. `androidx.hilt:hilt-work`·`Configuration.Provider`·`HiltWorkerFactory`·manifest
   초기화 제거 같은 앱 전역 배선을 피해 범위 축소. WorkManager 기본 초기화 사용 → manifest Worker 선언 불필요.

### 알려진 한계
- **주기 첫 실행 지연**: WorkManager 주기 작업 최소 간격은 15분이라 등록 직후 즉시 발화하지 않는다. 데모 즉시 확인은 `work-testing` 권장.
- **백그라운드 위치**: 워커는 `getLastKnownLocation()`(캐시)만 사용한다. 백그라운드 위치 권한/캐시 부재 시 해당 주기는 건너뛴다(다음 주기 재시도).
