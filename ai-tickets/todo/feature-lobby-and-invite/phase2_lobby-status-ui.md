# phase2: lobby-status-ui

## 🎯 문제 및 목표
- 대기실에서 초대한 친구들의 현재 승낙 현황(승낙/대기/거절)을 실시간으로 표시하고, 모든 인원이 승낙하는 순간 실시간 위치 공유 화면으로 자동 이동하는 트리거 구현.

## 📝 구체적 구현 방향
1. `LobbyScreen` (Compose) 구현:
   - 약속 정보 렌더링 카드
   - 참가자 프로필 리스트 및 상태 칩 (🟢 Accepted, 🟡 Pending, 🔴 Declined)
   - 전원 승낙 진행도 바 (예: 2/3명 완료)
2. `LobbyViewModel` (MVI) 작성:
   - `LobbyState`: `participants`, `isAllAccepted`, `groupStatus`
   - `LobbyEffect`: `NavigateToLiveTracking` (모두 승낙 시 발동하는 Side Effect)

## 🔍 검증 방법
- 마지막 수락자가 수락 버튼 클릭 시 `NavigateToLiveTracking` 부수효과가 발행되는지 유닛/UI 테스트.
