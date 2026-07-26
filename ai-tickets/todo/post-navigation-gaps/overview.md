# post-navigation-gaps — 네비게이션 연결 후 드러난 잔여 결함

## 🎯 목적

`app-navigation` 티켓으로 앱 셸을 연결한 뒤, 실기기 검증에서 확인된 4개의 잔여 결함을 정리한다.
모두 네비게이션 자체와는 무관하며, 각각 다른 레이어에 속한다.

## 📖 배경

`app-navigation` 완료 전까지 어떤 화면도 실행 경로가 없어 런타임 검증이 불가능했다.
연결 직후 처음으로 전체 플로우를 실기기에서 돌려본 결과 아래 항목들이 드러났다.

검증 환경: `Medium_Phone_API_35` 에뮬레이터 (Android 15), debug 빌드.

## 📋 하위 티켓

| Phase | 파일 | 요약 | 심각도 |
|---|---|---|---|
| 1 | `phase1_maps-api-key.md` | `MAPS_API_KEY` 미설정으로 지도가 회색 | 차단 |
| 2 | `phase2_bottomsheet-navbar-inset.md` | 핀 생성 CTA 버튼이 내비게이션 바에 가림 | 높음 |
| 3 | `phase3_applinks-assetlinks.md` | https 초대 링크가 앱이 아니라 브라우저로 열림 | 높음 |
| 4 | `phase4_arrival-simulation.md` | 도착 상태를 만들 수단이 없어 완료 화면 도달 불가 | 중간 |

## 🔗 선행 관계

서로 독립적이다. 다만 phase1(지도 API 키) 없이는 phase2·phase4의 실기기 검증이 불편하므로
phase1을 먼저 처리하는 것을 권한다.

## ✅ `app-navigation`에서 이미 수정된 항목 (참고)

네비게이션 검증 중 발견해 `app-navigation` 범위 안에서 함께 고친 것들이다. 재작업 불필요.

- `LobbyViewModel.observeGroup` / `LiveTrackingViewModel.startTracking`이
  `launchIn(viewModelScope)`으로 collector를 누적시키던 문제.
  화면이 dispose 후 재구성되면 Intent가 다시 들어와 collector가 2개가 되고,
  `NavigateToLiveTracking`이 중복 발행되어 백스택에 트래킹 목적지가 두 번 쌓였다.
  결과적으로 트래킹 화면에서 뒤로가기를 눌러도 대기실↔트래킹을 왕복해 앱을 벗어날 수 없었다.
  → 두 ViewModel에 `Job` 필드를 두고 재관찰 시 이전 Job을 취소하도록 수정.
    네비게이션 쪽에도 `launchSingleTop = true`를 방어적으로 추가.
