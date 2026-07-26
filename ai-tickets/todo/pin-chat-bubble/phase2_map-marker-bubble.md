# Phase 2: 맵 마커 말풍선 팝업 UI

## 🎯 목표
- 맵 상에 위치한 유저가 채팅을 쳤을 때, 해당 유저 핀(Marker) 위에 말풍선(Chat Bubble)이 나타나고 일정 시간 후 사라지는 기능을 구현합니다.

## 🛠️ 수정 대상
- `LiveTrackingScreen.kt` 내 `AnimatedParticipantMarker`
- 말풍선 UI 커스텀 컴포저블 신규 작성

## 📝 구체적인 구현 방향
- `MarkerComposable`의 자식 컴포넌트로 말풍선 UI를 추가합니다.
- `currentChatMessage`가 존재할 때만 렌더링되며, 팝업 애니메이션(`AnimatedVisibility` 등)을 적용합니다.
- 메시지가 렌더링된 후 `LaunchedEffect`를 사용해 약 3~4초 뒤 자동으로 메시지 상태를 `null`로 초기화하여 말풍선을 닫습니다.

## 🔍 검증 방법
- 채팅 전송 시 맵 위 유저 핀에 말풍선이 자연스럽게 나타났다가 지정된 시간 후 사라지는지 확인
