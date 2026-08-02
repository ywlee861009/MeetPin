# Phase 3: 푸시 알림(Notification) 발송

## 🎯 목표
- 출발해야 하는 시점이 오면 사용자에게 즉시 알림을 보냅니다.

## 🛠️ 수정 대상
- `NotificationHelper.kt` (또는 기존 알림 유틸)
- `DepartureCheckWorker.kt`

## 📝 구체적인 구현 방향
- 알림 채널(Notification Channel) 생성 (예: "출발 알림")
- Worker에서 조건이 충족되면 "지금 출발해야 늦지 않아요! (예상 소요시간: N분)" 메시지와 함께 로컬 알림(Notification) 띄우기
- 알림 터치 시 해당 약속의 맵(LiveTrackingScreen) 딥링크로 즉시 이동

## 🔍 검증 방법
- Mock 시간을 조작하여 출발 타이밍을 강제로 맞췄을 때 푸시 알림이 기기에 정상적으로 수신되는지 확인
