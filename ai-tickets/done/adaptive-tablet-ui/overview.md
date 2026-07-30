# 태블릿 및 대화면 UI 최적화 (adaptive-tablet-ui)

## 🎯 목적과 배경
현재 폰 환경에 맞춰져 있는 UI를 태블릿이나 폴더블 등 대화면 기기에서도 최적화된 형태로 보여주기 위한 작업입니다.
단순히 크기만 커지는 것이 아니라, 화면의 여백을 활용해 지도를 더 넓게 보여주고 리스트나 패널을 좌우(Side-by-side)로 분리하는 등 '태블릿스러운' Adaptive UX를 제공하는 것이 목표입니다.

## 📝 하위 티켓 목록
- `phase1_window-size-class.md`: Jetpack Compose WindowSizeClass 도입 및 화면 크기 측정 기반 마련 — ✅ 완료 (2026-07-30)
- `phase2_tracking-screen-tablet.md`: LiveTrackingScreen의 하단 시트를 태블릿 환경에서 좌/우측 패널로 분리 — ✅ 완료 (2026-07-30, 런타임 확인 대기)
- `phase3_other-screens-tablet.md`: 대기실(Lobby), 모임 생성 등 기타 화면의 반응형 레이아웃 대응 — ✅ 완료 (2026-07-30, 축소 범위)

> phase2·3에서 사이즈 클래스는 `com.kero.meetpin.core.designsystem.window.LocalWindowSizeClass`로 읽는다.
> (`isExpandedWidth` / `isCompactWidth` 확장 제공)

## 상태
- ✅ phase1(플럼빙)·phase2(트래킹 좌/우 분할)·phase3(InviteAccept 폭 제한) 완료 → **에픽 종료 (2026-07-30)**
- phase3는 축소 범위: 재설계로 `LobbyScreen`(→트래킹으로 통합, phase2 소관)·`CreatePinScreen`(전체화면 지도)
  전제가 소멸하여 `InviteAcceptScreen` 폭 제한만 수행. 상세는 phase3 파일 상단 참고.
- 공통 미결(별개 사안): 키보드 IME 인셋 미처리(`imePadding` 부재) — 태블릿 사이드 패널의
  채팅 입력이 키보드에 가릴 수 있음. 이 에픽 범위 밖, 필요 시 신규 티켓.
- 전 phase 공통: 컴파일·가드레일 검증 완료, 태블릿/폰 에뮬레이터 시각 확인은 미수행(런타임).

## 🔄 선행 관계
- phase1 ➔ phase2 ➔ phase3 순으로 진행
