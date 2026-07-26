# [overview] core-location-service

## 🎯 목적 및 배경
백그라운드 상태에서도 배터리 소모를 최적화하면서 유저의 GPS 좌표를 지속 수집하고, 약속 장소 반경 도착 감지(Geofencing)를 수행하는 `:core:location` 모듈을 구축합니다.

---

## 📋 하위 티켓 (Phases)
- **`phase1_fused-location-repository.md`**: `FusedLocationProviderClient` 기반 위치 수집 파이프라인
- **`phase2_foreground-location-service.md`**: `LocationTrackingService` (Foreground Service) 및 안심 알림 노출
- **`phase3_geofence-arrival-detector.md`**: 약속 장소 반경(50m) 진입 감지 알고리즘 및 이벤트 발송

---

## 🔗 선행 관계
- `core-domain-and-model` 완료 후 진행
