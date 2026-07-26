# Phase 2: 지각 시간 및 벌금 계산 로직

## 🎯 목표
- ViewModel에서 참가자들의 현재 지각 상태와 벌금을 실시간으로 계산하여 상태(`UiState`)로 노출합니다.

## 🛠️ 수정 대상
- `LiveTrackingViewModel.kt`
- `LiveTrackingContract.kt` (`ParticipantMarker`)

## 📝 구체적인 구현 방향
- `ParticipantMarker`에 `lateMinutes: Int`, `currentPenalty: Int` 상태 추가
- `LiveTrackingViewModel`에서 코루틴 `delay` 또는 Flow Timer를 활용해 1분(또는 테스트용 1초)마다 현재 시간과 `appointmentTime`을 비교
- 아직 도착하지 않은(`!isArrived`) 참가자 중 시간이 초과된 인원의 지각 시간과 벌금을 갱신

## 🔍 검증 방법
- 약속 시간이 지나면 ViewModel 상태의 `lateMinutes`와 `currentPenalty` 값이 주기적으로 증가하는지 확인
