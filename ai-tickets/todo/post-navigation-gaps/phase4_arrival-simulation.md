# Phase 4: 도착 상태 시뮬레이션 수단 확보 (완료 화면 도달)

## 🎯 목표

`CompletionScreen`을 실제로 도달·검증할 수 있게 만든다.

## 📖 문제

완료 화면으로의 이동 조건은 다음과 같다.

```kotlin
// LiveTrackingViewModel
val isAllArrived = group.participants.isNotEmpty() && group.participants.all { it.isArrived }
if (isAllArrived || group.status == GroupStatus.FINISHED) {
    sendEffect(LiveTrackingEffect.StopLocationService)
    sendEffect(LiveTrackingEffect.NavigateToCompletion(groupId))
}
```

그런데 `FakeMeetPinRepository`에는 **`Participant.isArrived`를 `true`로 바꾸는 경로가 전혀 없다.**
공개 API는 `createGroup` / `getGroupByInviteCode` / `updateInviteStatus` / `observeGroup` 4개이고,
어느 것도 도착 상태나 `GroupStatus.FINISHED`를 만들지 않는다.

`ArrivalDetector`가 `:core:location`에 있고 거리 계산도 구현되어 있으나,
그 판정 결과를 그룹 상태에 반영하는 배선이 없다.

**결과적으로 `CompletionScreen`과 도착 축하 이펙트(`ShowArrivalCelebration`)는
현재 어떤 조작으로도 도달할 수 없어 런타임 검증이 되지 않은 상태다.**
`app-navigation` 검증에서 이 두 경로만 확인하지 못했다.

## 🛠️ 수정 대상

- `android/core/domain/src/main/java/com/meetpin/core/domain/repository/MeetPinRepository.kt`
  — 도착 보고 API 추가
- `android/core/data/src/main/java/com/meetpin/core/data/repository/FakeMeetPinRepository.kt`
  — 구현
- `android/feature/tracking/src/main/java/.../LiveTrackingViewModel.kt`
  — `ArrivalDetector` 판정 결과를 리포지토리에 반영

## 📝 구체적인 구현 방향

### 1. 도착 보고 API

```kotlin
// MeetPinRepository
suspend fun reportArrival(groupId: String, userId: String): Result<Unit>
```

`FakeMeetPinRepository`에서 해당 참가자의 `isArrived`를 `true`로 바꾸고,
전원 도착 시 `status`를 `GroupStatus.FINISHED`로 전이시킨다.
`updateInviteStatus`가 전원 승낙 시 `ACTIVE`로 전이시키는 것과 같은 패턴을 따른다.

### 2. ArrivalDetector 연결

`LiveTrackingViewModel`이 이미 `arrivalDetector.calculateDistance`로 거리를 구하고 있다.
이 거리가 도착 반경 이내이고 아직 `isArrived`가 아니면 `reportArrival`을 호출한다.
매 emission마다 중복 호출되지 않도록 이미 보고한 userId를 기억한다.

### 3. 검증 수단

에뮬레이터에서 위치를 약속 장소로 이동시켜 도착을 만든다.

```bash
# 핀을 서울시청(37.5666805, 126.9784147)에 찍고
adb emu geo fix 126.9784147 37.5666805
```

`DefaultLocationClient`가 Fused Location을 쓰므로 `geo fix`가 반영되는지 먼저 확인한다.
반영되지 않으면 개발용 진입점(예: 트래킹 화면의 debug-only "도착 처리" 버튼)을 두는 것도 방법이다.
단 프로덕션 경로에 남지 않도록 `BuildConfig.DEBUG` 가드를 반드시 건다
(`build-restoration` phase4에서 프로덕션에 박힌 Mock 주입을 제거한 이력이 있다).

## 🔍 검증 방법

- 실시간 트래킹 화면에서 참가자가 약속 장소에 도착하면
  `🎉 {닉네임}님이 도착했습니다!` 스낵바가 뜬다.
- 참가자 현황이 `0 / 2 도착` → `2 / 2 도착`으로 바뀐다.
- 전원 도착 시 완료 화면으로 이동하고, 위치 공유 포그라운드 알림이 사라진다.
  ```bash
  adb shell dumpsys activity services com.meetpin.app   # (nothing) 이어야 한다
  ```
- 완료 화면에서 홈으로 이동 시 핀 생성 화면으로 돌아가고, 뒤로가기로 앱이 종료된다
  (완료·트래킹 화면으로 복귀하지 않는다).
- 도착 보고가 중복 호출되지 않는다 (스낵바가 한 번만 뜬다).
