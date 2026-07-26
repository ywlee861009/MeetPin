# phase3: arrival-effect-and-completion

## 🎯 문제 및 목표
- 약속 장소도착 시 축하 이펙트 노출 및 전원 도착 또는 모임 종료 버튼 클릭 시 위치 공유 서비스를 완전히 파기하고 모임 완료 화면으로 이동.

## 📝 구체적 구현 방향
1. 도착 축하 애니메이션 (Lottie/Compose 이펙트) 및 토스트 팝업.
2. `LocationTrackingService` 종료 커맨드 전달.
3. `CompletionScreen` 이동 및 위치 수집 완전히 중단되었음을 알림.

## 🔍 검증 방법
- 도착 이벤트 발생 시 축하 이펙트 노출 및 백그라운드 위치 서비스가 깔끔하게 종료되는지 확인.
