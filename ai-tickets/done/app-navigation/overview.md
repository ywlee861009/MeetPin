# app-navigation — 앱 셸(네비게이션) 구축

## 🎯 목적

구현된 7개 화면을 실제 앱에서 도달 가능하게 만든다.

## 📖 배경

`feature:map` / `feature:lobby` / `feature:tracking` 의 화면 7개가 모두 구현되어 있으나,
`app` 모듈의 `MainActivity` 는 Android Studio Compose 템플릿의 `Greeting()` 을 그대로 표시하고 있다.
그 결과 앱을 실행하면 **"Welcome to MeetPin!" 텍스트만 보이고 기능에 접근할 수 없다.**

지금까지의 모든 티켓 검증 기준이 `./gradlew :app:assembleDebug` 성공뿐이었고,
"UI에서 도달 가능한가"를 확인하는 단계가 없어 이 상태가 계속 누락되었다.

### 현재 누락 항목

| 항목 | 상태 |
|---|---|
| `NavHost` / 네비게이션 그래프 | 코드베이스 전체에 0건 |
| `androidx.navigation:navigation-compose` | 미선언 (`hilt-navigation-compose` transitive만 존재) |
| 런타임 위치·알림 권한 요청 | 없음 (`LocationPermissionHelper` 호출부 0건) |
| `LocationTrackingService.start()` | 호출부 0건 (`stop()` 만 호출됨) |
| 딥링크 진입 | Manifest intent-filter는 있으나 `DeepLinkHandler` 호출부 0건 |

### 화면 간 연결 관계 (구현된 콜백 기준)

```
CreatePinScreen
  └─ PinCreateEffect.NavigateToInviteShare(groupId, inviteCode)
       └─ InviteShareScreen(groupTitle, inviteCode)
            └─ onNavigateToLobby()
                 └─ LobbyScreen(groupId)
                      └─ LobbyEffect.NavigateToLiveTracking(groupId)
                           └─ LiveTrackingScreen(groupId)
                                └─ LiveTrackingEffect.NavigateToCompletion(groupId)
                                     └─ CompletionScreen(groupId, onNavigateToHome)

딥링크 진입:
  https://meetpin.app/invite/{code}  또는  meetpin://invite?code={code}
       └─ InviteAcceptScreen(inviteCode)
            └─ InviteAcceptEffect.NavigateToLobby(groupId) → LobbyScreen
            └─ InviteAcceptEffect.NavigateBack
```

## 📋 하위 티켓

| Phase | 파일 | 요약 |
|---|---|---|
| 1 | `phase1_navhost-and-routes.md` | `navigation-compose` 의존성, Route 정의, `MeetPinNavHost`, `MainActivity` 연결 |
| 2 | `phase2_runtime-permission-flow.md` | 위치·알림 런타임 권한 요청 흐름, `LocationTrackingService` 시작 연결 |
| 3 | `phase3_deeplink-entry.md` | 딥링크 cold/warm start 진입 처리 |

## 🔗 선행 관계

- phase1 → phase2 → phase3 (순차)
- phase1 완료 없이는 phase2/3의 검증이 불가능하다 (권한 결과를 주입할 대상 화면, 딥링크 이동 대상 목적지가 phase1에서 만들어짐).

## ⚠️ 시작 화면 결정

시작 목적지는 **`CreatePinScreen`** 으로 한다.

"내 모임 목록" 성격의 홈 화면은 구현되어 있지 않고 `MeetPinRepository` 에도 그룹 목록 조회 API가 없다
(`createGroup` / `getGroupByInviteCode` / `updateInviteStatus` / `observeGroup` 4개뿐).
홈 화면을 신규 구현하지 않고도 전체 플로우를 끝까지 검증할 수 있는 최단 경로가 `CreatePinScreen` 진입이다.
홈 화면은 별도 티켓으로 분리한다.
