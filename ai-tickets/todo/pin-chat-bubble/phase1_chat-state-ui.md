# Phase 1: 채팅 입력 UI 및 상태 모델

## 🎯 목표
- 화면 하단에 채팅을 입력할 수 있는 간단한 바(TextField)를 추가합니다.
- 채팅 입력을 ViewModel 상태와 연동하여 맵 컴포넌트로 전달할 준비를 마칩니다.

## 🛠️ 수정 대상
- `LiveTrackingScreen.kt` (채팅 입력 UI 추가)
- `LiveTrackingViewModel.kt` (상태 및 Intent 추가)
- `LiveTrackingState.kt` (채팅 관련 상태 추가)

## 📝 구체적인 구현 방향
- 화면 하단 참가자 상태 시트 위쪽에 채팅 입력 필드를 배치합니다.
- `ParticipantMarker` 혹은 관련 UI 상태 객체에 `currentChatMessage: String?`와 `messageTimestamp: Long?` 상태를 추가하여 일시적인 말풍선 상태를 유지합니다.

## 🔍 검증 방법
- 화면 하단에 채팅 입력 창이 보이고, 입력 후 전송 시 상태값(ViewModel)이 업데이트 되는지 확인
