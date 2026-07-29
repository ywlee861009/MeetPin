# phase4 (선택): 친구 이동 애니메이션 + 도착 상태 데모

## 문제 / 목표
정적 위치만으로도 phase1~3이면 데모의 핵심은 충족된다. 여기서는 "친구가 목적지로
점점 다가오는" 보간 애니메이션과 "도착"으로 전환되는 모습까지 보고 싶을 때를 다룬다.
`post-navigation-gaps`의 phase4(도착 시뮬)와 목적이 겹치므로 함께 처리해도 된다.

## 배경
- `AnimatedParticipantMarker`는 `targetPosition`이 바뀌면 1초 tween으로 마커를 보간 이동한다.
  즉 데모 위치 소스가 좌표를 갱신해 방출하면 마커가 저절로 움직인다.
- 도착 표현(`MeetPinAvatar` 체크마크, 시트 초록 라벨)은 `Participant.isArrived`로 구동된다.
  현재 이 값을 true로 만들 경로가 없다.

## 구현 방향
1. **이동**: 데모 위치 소스(phase2의 `DemoScenarioController` 또는 `updateGroupLocations`)가
   일정 주기(`delay`)로 친구 좌표를 목적지 핀 쪽으로 조금씩 보간해 방출한다.
   - 예: 강남 친구를 15초에 걸쳐 강남→핀 경로의 여러 지점으로 갱신.
   - 실제 GPS(호스트)와 섞이지 않도록 친구 userId로만 갱신.
2. **도착**: 목적지 반경 이내(`ArrivalDetector`)에 들어오면 해당 친구 `isArrived=true`로.
   - 데모에서는 위치 소스가 "도착 좌표"를 방출하고, 상태 반영 경로를 정한다:
     - 안 A(위치 기반): ViewModel이 거리 < 임계값이면 마커 표시를 도착으로 계산.
       단 `isArrived`는 `Participant`(그룹 모델) 필드라 위치만으로는 안 바뀐다 → 아래 택1.
     - 안 B(그룹 모델 갱신): `MeetPinRepository`에 데모용 `markArrived(groupId, userId)`를
       추가해 `Participant.isArrived`를 갱신 → `observeGroup` 흐름으로 UI 반영(정석).
   - `post-navigation-gaps/phase4_arrival-simulation`과 설계를 통일할 것.

## 주의
- 이동 갱신 주기가 짧으면 배터리/리컴포지션 부담. 데모용이므로 1~2초 간격이면 충분.
- 도착 로직을 `MeetPinRepository`에 넣으면 인터페이스가 커진다. 데모 전용임을 주석으로 명시.

## 검증
- [ ] 친구 마커가 목적지로 부드럽게 이동(보간)하는 것 확인
- [ ] 반경 진입 시 아바타가 체크마크로, 시트 라벨이 "도착 완료!"로 전환
- [ ] `DesignSystemGuardrailTest` 그린 유지
