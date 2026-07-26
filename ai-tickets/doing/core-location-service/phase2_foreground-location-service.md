# phase2: foreground-location-service

## 🎯 문제 및 목표
- Android OS에 의한 위치 서비스 강제 종료를 방지하고 사생활 노출을 명확히 알리기 위한 Android Foreground Service 구현.

## 📝 구체적 구현 방향
1. `LocationTrackingService` (Foreground Service) 클래스 구현 (`FOREGROUND_SERVICE_TYPE_LOCATION`).
2. 상단 상시 노출 Notification 구현 ("MeetPin: 실시간 위치를 친구들과 공유 중입니다.").
3. 서비스 시작/중지 커맨드 Intent 수신 및 수명주기 처리.

## 🔍 검증 방법
- 서비스 실행 시 상단 노출 알림창 렌더링 및 백그라운드 앱 전환 후에도 좌표 수집 지속 확인.
