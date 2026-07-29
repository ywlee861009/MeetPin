# livetracking-adopt-designsystem

## 🎯 목적 / 배경
디자인 시스템(`:core:designsystem`) 이관에서 `LiveTrackingScreen`·`ParticipantStatusSheet`만
남았다. 이 두 파일은 미커밋 WIP(디버그 모의 코드)가 있어 이번 이관에서 보류했다.

가드레일 테스트(`DesignSystemGuardrailTest`)가 이 두 파일을 **임시 예외**(`pendingMigration`)로
두고 있으므로, 이관 완료 시 예외를 제거해 규칙이 전면 적용되게 해야 한다.

## 선행 관계
- WIP(상대 수락/채팅 시뮬 등) 커밋 또는 되돌리기가 먼저. (커밋이 섞이지 않도록)

## 하위 티켓
- `phase1_livetracking-screen.md` — 초대 링크/디버그 버튼 → DS 버튼, 채팅 말풍선 → MeetPinChatBubble,
  참가자 시트 → ParticipantRow, 상단 라이브 바 → LiveBadge, 채팅 입력 → (DS TextField 신설 검토)
- `phase2_remove-guardrail-exception.md` — `pendingMigration` 예외 제거 후 가드레일 그린 확인

## 상태
- ⏳ 대기
