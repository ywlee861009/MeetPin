# 출발 알림 (ETA 기반) (eta-departure-alert)

## 🎯 목적과 배경
모임 시간에 늦지 않도록 각 사용자의 현재 위치에서 목적지까지의 이동 시간을 계산하여, 적절한 시간에 "지금 출발해야 늦지 않아요!"라는 푸시 알림을 제공합니다.

## 📝 하위 티켓 목록
- `phase1_eta-calculation-logic.md`: 목적지까지의 도보/대중교통 소요 시간(ETA) 계산 로직 연동
- `phase2_departure-time-worker.md`: 백그라운드에서 주기적으로 출발 여부를 체크하는 WorkManager 구성
- `phase3_push-notification.md`: 출발 임박 시 로컬 푸시 알림 트리거 및 UI 연동

## 🔄 선행 관계
- phase1 ➔ phase2 ➔ phase3 순으로 진행
