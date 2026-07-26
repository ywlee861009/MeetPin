# [overview] feature-live-tracking

## 🎯 목적 및 배경
약속 참가자 전원의 실시간 좌표를 받아 지도 상에 아바타 마커 부드러운 애니메이션으로 표시하고, ETA(예상소요시간) 및 도착 축하 이펙트/모임 종료를 다루는 `:feature:tracking` MVI 화면을 구축합니다.

---

## 📋 하위 티켓 (Phases)
- **`phase1_live-map-markers.md`**: 수신 좌표 기반 커스텀 아바타 마커 부드러운 이동 애니메이션 (Compose)
- **`phase2_eta-and-distance-ui.md`**: 하단 참가자 상태 바 및 남은 거리/ETA 표시 UI
- **`phase3_arrival-effect-and-completion.md`**: 도착 핑 축하 폭죽 이펙트 및 위치 공유 자동 파기 완료 화면

---

## 🔗 선행 관계
- `feature-pin-creation-and-map` 및 `core-location-service` 완료 후 진행
