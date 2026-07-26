# Phase 2: 출발 스케줄러 (WorkManager) 연동

## 🎯 목표
- 앱이 백그라운드에 있을 때도 출발 시간을 체크할 수 있도록 Android WorkManager를 설정합니다.

## 🛠️ 수정 대상
- `DepartureCheckWorker.kt` (신규 파일)
- `AndroidManifest.xml` (Worker 선언 필요 시)

## 📝 구체적인 구현 방향
- `PeriodicWorkRequestBuilder`를 사용해 약속 시간 1~2시간 전부터 15분 간격으로 Worker 실행
- Worker 내에서 현재 위치를 수집 후 `EtaCalculator`를 호출해 `현재 시간 + ETA >= 약속 시간` 인지 판단
- 조건 충족 시 알림 플래그 반환

## 🔍 검증 방법
- WorkManager 디버깅을 통해 주기적으로 Worker가 실행되고 조건(출발 임박)을 올바르게 판단하는지 로그 확인
