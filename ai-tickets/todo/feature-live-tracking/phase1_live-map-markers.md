# phase1: live-map-markers

## 🎯 문제 및 목표
- 실시간 좌표 갱신 시 지도 위 마커가 갑자기 순간 이동하지 않고 부드럽게 좌표를 이동(Marker Interpolation)하는 Compose 지도 화면 구현.

## 📝 구체적 구현 방향
1. `LiveTrackingScreen` (Compose GoogleMap) 구현.
2. `animateLatLngAsState` 또는 `Animatable`을 활용한 LatLng 좌표 부드러운 보간 애니메이션.
3. 참가자별 프로필 이미지가 포함된 커스텀 마커 렌더링 (`MarkerInfoContent`).

## 🔍 검증 방법
- 위치 업데이트 이벤트를 연속 전달할 때 마커가 부드럽게 이동하는지 화면 확인.
