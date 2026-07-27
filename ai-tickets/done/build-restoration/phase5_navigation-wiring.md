# Phase 5: 네비게이션 배선 (구현된 화면 연결)

> **⚠️ 이 phase는 `ai-tickets`의 `app-navigation` 티켓으로 대체되었습니다 (SUPERSEDED).**
>
> 같은 범위를 3개 phase로 세분화하고, 아래와 다른 두 가지 결정을 반영했습니다.
> - **시작 목적지**: 아래 문서는 `map`이지만 `app-navigation`은 `createPin`으로 확정했습니다.
>   `MapScreen`은 ViewModel이 없는 표시 전용 화면이라 `CreatePinScreen`과 지도 UI가 중복되고,
>   진입 후 즉시 `createPin`으로 넘기는 껍데기 역할만 하게 됩니다.
> - **딥링크 처리 방식**: 아래 문서는 `navDeepLink`를 제안하지만 `app-navigation`은
>   이미 구현되어 있는 `DeepLinkHandler`를 사용합니다. `DeepLinkHandler`는
>   `meetpin://invite?code=` 커스텀 스킴까지 파싱하므로 두 형식을 한 곳에서 처리할 수 있습니다.
>
> 아래 내용은 기록 목적으로 남겨둡니다. 구현은 `app-navigation` 티켓을 따르세요.

> phase3 진행 중 발견된 항목입니다. 최초 계획에는 없었습니다.

## 🎯 목표
- `MainActivity`의 AGP 템플릿 화면(`Greeting("MeetPin")`)을 실제 앱 진입점으로 교체한다.
- 이미 구현되어 있으나 **도달 불가 상태인** 화면들을 `NavHost`로 연결한다.
- 딥링크(`https://meetpin.app/invite/{code}`)가 초대 수락 화면으로 진입하도록 한다.
- 전체 플로우(핀 생성 → 초대 공유 → 수락 → 대기실 → 트래킹 → 완료)를 실기기에서 확인한다.

## 🛠️ 수정 대상
- `app/src/main/java/com/meetpin/app/MainActivity.kt` (템플릿 `Greeting`/`GreetingPreview` 제거)
- `app/src/main/java/com/meetpin/app/navigation/` (신규)
  - `MeetPinRoute.kt` — 라우트 정의
  - `MeetPinNavHost.kt` — NavHost 구성
- `app/build.gradle.kts` — `androidx.navigation:navigation-compose` 추가
- `gradle/libs.versions.toml` — navigation-compose 별칭 추가
- `feature/lobby/DeepLinkHandler.kt` — NavHost 딥링크와의 연결 방식 확인

## 📝 배경 (현재 상태)
`grep`으로 확인한 결과 프로젝트 전체에 `NavHost` / `rememberNavController`가 **하나도 없습니다.**
`MainActivity`는 AGP 프로젝트 템플릿의 `Greeting` 컴포저블을 그대로 표시하고 있어,
아래 화면들이 모두 코드에만 존재하고 실행 경로가 없습니다.

- `MapScreen`, `CreatePinScreen`, `InviteShareScreen` (`:feature:map`)
- `LobbyScreen`, `InviteAcceptScreen` (`:feature:lobby`)
- `LiveTrackingScreen`, `CompletionScreen` (`:feature:tracking`)

각 화면의 `UiEffect`에는 이미 이동 의도가 정의되어 있으므로(예:
`PinCreateEffect.NavigateToInviteShare`, `LobbyEffect.NavigateToLiveTracking`,
`LiveTrackingEffect.NavigateToCompletion`), 이를 실제 `navController` 호출로 연결하면 된다.

## 📝 구체적인 구현 방향
- 라우트: `map` → `createPin` → `inviteShare/{groupId}/{inviteCode}` → `lobby/{groupId}`
  → `tracking/{groupId}` → `completion/{groupId}`, 그리고 딥링크 진입점 `invite/{inviteCode}`
- 시작 목적지는 `map`
- 각 Screen의 `effect` 수집부에서 `navController.navigate(...)`를 호출한다.
  화면 컴포저블이 `navController`를 직접 알지 못하도록 `onNavigateTo...` 람다를 파라미터로 받는 방식을 권장.
- 트래킹 완료 후에는 `popUpTo(map) { inclusive = false }`로 백스택을 정리한다.
- 딥링크는 `composable(route, deepLinks = listOf(navDeepLink { uriPattern = "https://meetpin.app/invite/{inviteCode}" }))`
  로 선언한다. Manifest의 App Links intent-filter는 이미 존재한다.
- 위치 권한 요청 시점(트래킹 진입 전)을 어디서 처리할지 함께 결정한다
  (`LocationPermissionHelper`가 이미 `:core:location`에 있음).

## 🔍 검증 방법
```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
adb shell am start -a android.intent.action.VIEW -d "https://meetpin.app/invite/MP0001"
```
- 앱 실행 시 지도 화면이 첫 화면으로 표시된다.
- 지도 탭 → 핀 생성 → 초대 공유 → 수락 → 대기실 → 트래킹 → 완료까지 이동이 끊기지 않는다.
- 위 `adb` 딥링크 명령으로 초대 수락 화면이 직접 열린다.
- 트래킹 화면에서 참가자 마커와 지각 표시가 실제로 렌더링된다
  (phase4에서 계산 로직은 단위 테스트로만 검증된 상태).
- 이 단계에서 Hilt ViewModel 주입이 **런타임으로도** 처음 검증된다.
