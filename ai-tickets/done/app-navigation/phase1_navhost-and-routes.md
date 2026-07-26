# Phase 1: NavHost 및 Route 정의

## 🎯 목표

- `androidx.navigation:navigation-compose` 의존성을 명시적으로 선언한다.
- 6개 목적지(createPin / inviteShare / lobby / liveTracking / completion / inviteAccept)의 Route를 정의한다.
- `MeetPinNavHost` 를 만들어 구현된 화면들을 실제로 연결한다.
- `MainActivity` 의 템플릿 `Greeting()` 을 제거하고 `MeetPinNavHost` 로 교체한다.

## 🛠️ 수정 대상

- `android/gradle/libs.versions.toml` — `navigation-compose` 별칭 추가
- `android/app/build.gradle.kts` — 의존성 추가
- `android/app/src/main/java/com/meetpin/app/navigation/MeetPinRoute.kt` (신규)
- `android/app/src/main/java/com/meetpin/app/navigation/MeetPinNavHost.kt` (신규)
- `android/app/src/main/java/com/meetpin/app/MainActivity.kt` — `Greeting()` 제거
- `android/feature/map/src/main/java/com/meetpin/feature/map/PinCreateContract.kt` — 효과에 `groupTitle` 추가
- `android/feature/map/src/main/java/com/meetpin/feature/map/CreatePinViewModel.kt` — 효과 생성부 갱신
- `android/feature/map/src/main/java/com/meetpin/feature/map/CreatePinScreen.kt` — 콜백 시그니처 갱신
- `android/feature/map/src/main/java/com/meetpin/feature/map/InviteShareScreen.kt` — 미사용 import 정리

## 📝 구체적인 구현 방향

### 1. Route 정의

```kotlin
sealed interface MeetPinRoute {
    val pattern: String
}
```

각 목적지는 `pattern`(NavHost 등록용)과 인자를 받아 실제 경로를 만드는 함수를 함께 갖는다.

| 목적지 | pattern |
|---|---|
| CreatePin | `createPin` |
| InviteShare | `inviteShare/{groupId}/{inviteCode}?title={groupTitle}` |
| Lobby | `lobby/{groupId}` |
| LiveTracking | `liveTracking/{groupId}` |
| Completion | `completion/{groupId}` |
| InviteAccept | `inviteAccept/{inviteCode}` |

`groupTitle` 은 사용자가 입력한 한글/공백/슬래시가 섞일 수 있으므로 **path argument가 아닌 query
argument로 두고 `Uri.encode` 로 인코딩**한다. path argument에 인코딩된 `%2F` 를 넣으면
Navigation 라이브러리에서 매칭이 깨질 수 있다.

### 2. `groupTitle` 전달 경로 확보

`InviteShareScreen` 은 `groupTitle` 을 요구하지만 `PinCreateEffect.NavigateToInviteShare` 는
`groupId`, `inviteCode` 만 담고 있다. `InviteShareScreen` 은 ViewModel이 없는 stateless 화면이므로
조회로 해결할 수 없다. 효과에 `groupTitle` 을 추가한다.

```kotlin
data class NavigateToInviteShare(
    val groupId: String,
    val groupTitle: String,
    val inviteCode: String
) : PinCreateEffect
```

`CreatePinViewModel.submitPin()` 의 `onSuccess` 블록에서 `group.title` 을 함께 넘긴다.
`CreatePinScreen` 의 콜백은
`onNavigateToInviteShare: (groupId: String, groupTitle: String, inviteCode: String) -> Unit` 로 바꾼다.

### 3. 백스택 정책

| 이동 | 정책 |
|---|---|
| CreatePin → InviteShare | 일반 push (뒤로가기로 핀 수정 복귀 허용) |
| InviteShare → Lobby | `popUpTo(createPin) { inclusive = true }` — 생성 완료 후 뒤로가기로 재생성 방지 |
| Lobby → LiveTracking | `popUpTo(lobby) { inclusive = true }` — 전원 승낙 후 대기실 복귀 무의미 |
| LiveTracking → Completion | `popUpTo(liveTracking) { inclusive = true }` — 종료 후 추적 화면 복귀 방지 |
| Completion → Home | `popUpTo(createPin) { inclusive = true }` 로 createPin 재시작 |
| InviteAccept → Lobby | `popUpTo(inviteAccept) { inclusive = true }` |

### 4. MainActivity

`Greeting` / `GreetingPreview` composable을 삭제하고 `MeetPinNavHost()` 를 호출한다.
`enableEdgeToEdge()` 는 유지한다. 각 화면이 자체 `Scaffold` 를 갖고 있으므로
`MainActivity` 에서 `Scaffold` 를 중복으로 감싸지 않는다.

## 🔍 검증 방법

```bash
cd android && ./gradlew :app:assembleDebug
```

- 빌드 성공.
- 실기기/에뮬레이터 설치 후:
  - 앱 실행 시 "Welcome to MeetPin!" 대신 지도 + 핀 생성 화면이 나온다.
  - 지도 탭 → BottomSheet 확장 → 제목/날짜/시간 입력 → 생성 → 초대 공유 화면 진입.
  - 초대 공유 화면에 입력한 제목이 그대로 표시된다 (한글 제목 포함).
  - "대기실로 이동 →" → 대기실 → 전원 승낙 시 실시간 추적 화면 → 전원 도착 시 완료 화면.
  - 완료 화면에서 홈으로 → 핀 생성 화면으로 복귀하며 뒤로가기 시 앱 종료.
- `grep -rn "Greeting" android/app/src` 결과 0건.
