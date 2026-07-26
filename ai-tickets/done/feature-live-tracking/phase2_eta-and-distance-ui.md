# phase2: eta-and-distance-ui

## 🎯 문제 및 목표
- 하단 참가자 시트에 멤버별 약속 장소까지의 남은 거리(m/km) 및 예상 소요시간(ETA) 카드 표시.

## 📝 구체적 구현 방향
1. `ParticipantStatusSheet` UI 구성.
2. 멤버 선택 시 해당 마커 위치로 지도 카메라 이동 (`CameraPositionState.animate`).
3. 상단 라이브 공유 안내 바 및 "위치 공유 끄기" 버튼 조작.

## 🔍 검증 방법
- 카드 클릭 시 지도 카메라가 지정된 멤버 위치로 정상 이동하는지 확인.
