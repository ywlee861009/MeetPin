# post-navigation-gaps — 네비게이션 연결 후 드러난 잔여 결함

## 🎯 목적

`app-navigation` 티켓으로 앱 셸을 연결한 뒤, 실기기 검증에서 확인된 4개의 잔여 결함을 정리한다.
모두 네비게이션 자체와는 무관하며, 각각 다른 레이어에 속한다.

## 📖 배경

`app-navigation` 완료 전까지 어떤 화면도 실행 경로가 없어 런타임 검증이 불가능했다.
연결 직후 처음으로 전체 플로우를 실기기에서 돌려본 결과 아래 항목들이 드러났다.

검증 환경: `Medium_Phone_API_35` 에뮬레이터 (Android 15), debug 빌드.

## 📋 하위 티켓

| Phase | 파일 | 요약 | 심각도 | 상태 |
|---|---|---|---|---|
| 1 | `phase1_maps-api-key.md` | `MAPS_API_KEY` 미설정으로 지도가 회색 | 차단 | ✅ 완료 (done) |
| 2 | ~~`phase2_bottomsheet-navbar-inset.md`~~ | ~~핀 생성 CTA 버튼이 내비게이션 바에 가림~~ | 높음 | ❌ 폐지 (아래 참고) |
| 3 | `phase3_applinks-assetlinks.md` | https 초대 링크가 앱이 아니라 브라우저로 열림 | 높음 | ⏳ 대기 (웹서버 의존, 보류) |
| 4 | `phase4_arrival-simulation.md` | 도착 상태를 만들 수단이 없어 완료 화면 도달 불가 | 중간 | ⏳ 대기 |

### ❌ phase2 폐지 사유 (2026-07-29)

작성 이후의 화면 재설계로 티켓의 전제가 소멸하여 폐지한다. 실제 코드 대조 결과:

- **CreatePinScreen**: `BottomSheetScaffold`가 제거되고 "약속 만들기"가 **화면 중앙 버튼**
  (즉시 공유 플로우)으로 재설계됨 → 내비바에 잘릴 하단 CTA·텍스트 입력 자체가 없음.
- **CompletionScreen**: 아직 미구현(파일 없음) → 검증 불가. (완료 화면 자체는 phase4 소관)
- **LiveTrackingScreen**: 루트 `Column`에 `Scaffold` `innerPadding`(systemBars)을 적용 중이라
  내비바 inset이 **이미 반영**됨.

> 참고(미결 관찰): 코드 전역에 `imePadding()`/`WindowInsets.ime` 사용이 없고 manifest에
> `windowSoftInputMode`도 없다. `LiveTrackingScreen`의 `ChatInputBar`(채팅 입력)는 키보드가
> 뜨면 가려질 수 있다. 다만 이는 phase2의 "내비바 inset" 범위와 다른 별개 사안이며, 필요 시
> 신규 티켓으로 다룬다.

## 🔗 선행 관계

서로 독립적이다. 다만 phase1(지도 API 키) 없이는 phase4의 실기기 검증이 불편하므로
phase1을 먼저 처리하는 것을 권한다. (phase1 완료됨)

## ✅ `app-navigation`에서 이미 수정된 항목 (참고)

네비게이션 검증 중 발견해 `app-navigation` 범위 안에서 함께 고친 것들이다. 재작업 불필요.

- `LobbyViewModel.observeGroup` / `LiveTrackingViewModel.startTracking`이
  `launchIn(viewModelScope)`으로 collector를 누적시키던 문제.
  화면이 dispose 후 재구성되면 Intent가 다시 들어와 collector가 2개가 되고,
  `NavigateToLiveTracking`이 중복 발행되어 백스택에 트래킹 목적지가 두 번 쌓였다.
  결과적으로 트래킹 화면에서 뒤로가기를 눌러도 대기실↔트래킹을 왕복해 앱을 벗어날 수 없었다.
  → 두 ViewModel에 `Job` 필드를 두고 재관찰 시 이전 Job을 취소하도록 수정.
    네비게이션 쪽에도 `launchSingleTop = true`를 방어적으로 추가.
