# phase3: geofence-arrival-detector

## 🎯 문제 및 목표
- 수집된 현재 위치와 약속 핀 위치 간의 거리(Haversine 알고리즘 또는 `Location.distanceTo`)를 계산하여 50m 이내 진입 시 도착(Arrived) 처리.

## 📝 구체적 구현 방향
1. `ArrivalDetector` 도메인 컴포넌트 개발.
2. 약속 장소 반경(50m) 진입 이벤트 감지 시 `Participant.isArrived = true` 및 서버/상태에 도착 핑 보냄.

## 🔍 검증 방법
- 좌표 이동 단위 테스트를 통해 50m 외곽 ➔ 50m 내 진입 시 도착 이벤트를 정확히 방출하는지 검증.
