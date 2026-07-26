# phase1: fused-location-repository

## 🎯 문제 및 목표
- Google Play Services `FusedLocationProviderClient`를 사용하여 실시간 GPS 좌표를 `Flow<LocationUpdate>`로 방출하는 위치 리포지토리 개발.

## 📝 구체적 구현 방향
1. `DefaultLocationClient` 구현 (가변 주기 업데이트 - 이동 거리 5m 이상 또는 3~5초 간격).
2. 위치 정확도(Accuracy < 20m) 필터링 및 노이즈 제거 로직 추가.
3. 위치 권한 허용 상태 체크 및 에러 처리.

## 🔍 검증 방법
- 가상 GPX/KML 트랙 또는 에뮬레이터 Location 컨트롤러를 통해 좌표 흐름 검증.
