# phase1: invite-acceptance-dialog

## 🎯 문제 및 목표
- 딥링크 URL(`https://meetpin.app/invite/{code}`)을 통해 들어온 유저에게 약속 장소/시간 카드를 보여주고 [승낙하기] / [거절하기] 입력을 받는 인터랙션 구현.

## 📝 구체적 구현 방향
1. `DeepLinkHandler` 작성 (Intent Data 파싱).
2. `InviteAcceptDialog` / `InviteAcceptScreen` (Compose) 구현.
3. 승낙/거절 클릭 시 `Participant.inviteStatus` 업데이트 API/Repository 호출.

## 🔍 검증 방법
- adb shell `am start -a android.intent.action.VIEW -d "https://meetpin.app/invite/abc123"` 호출 시 초대 화면 노출 검증.
