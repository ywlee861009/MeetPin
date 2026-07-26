# 지각비 & 벌칙금 타이머 (late-penalty-timer)

## 🎯 목적과 배경
약속 시간이 지나도 도착하지 않은 참가자에게 경각심(재미 요소)을 주기 위해 지각비와 타이머 UI를 제공합니다. 참가자 마커 위에 실시간으로 늘어나는 지각 시간과 벌금을 표시하여 모임의 재미를 더합니다.

## 📝 하위 티켓 목록
- `phase1_late-status-model.md`: 약속 시간 및 벌금 규칙(분당 벌금 등) 데이터 모델 추가
- `phase2_penalty-calculator.md`: 현재 시간과 약속 시간을 비교하여 지각 시간 및 누적 벌금 계산 로직 구현
- `phase3_marker-penalty-ui.md`: 지도 마커(`AnimatedParticipantMarker`) 상단 또는 스니펫에 지각 페널티 UI 표시

## 🔄 선행 관계
- phase1 ➔ phase2 ➔ phase3 순으로 진행
