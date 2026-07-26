# 태블릿 및 대화면 UI 최적화 (adaptive-tablet-ui)

## 🎯 목적과 배경
현재 폰 환경에 맞춰져 있는 UI를 태블릿이나 폴더블 등 대화면 기기에서도 최적화된 형태로 보여주기 위한 작업입니다.
단순히 크기만 커지는 것이 아니라, 화면의 여백을 활용해 지도를 더 넓게 보여주고 리스트나 패널을 좌우(Side-by-side)로 분리하는 등 '태블릿스러운' Adaptive UX를 제공하는 것이 목표입니다.

## 📝 하위 티켓 목록
- `phase1_window-size-class.md`: Jetpack Compose WindowSizeClass 도입 및 화면 크기 측정 기반 마련
- `phase2_tracking-screen-tablet.md`: LiveTrackingScreen의 하단 시트를 태블릿 환경에서 좌/우측 패널로 분리
- `phase3_other-screens-tablet.md`: 대기실(Lobby), 모임 생성 등 기타 화면의 반응형 레이아웃 대응

## 🔄 선행 관계
- phase1 ➔ phase2 ➔ phase3 순으로 진행
