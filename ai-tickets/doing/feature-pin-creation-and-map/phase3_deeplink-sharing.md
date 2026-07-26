# phase3: deeplink-sharing

## 🎯 문제 및 목표
- 약속 생성 완료 후 친구들에게 보낼 고유 딥링크(`https://meetpin.app/invite/{code}`)를 생성하고 공유 기능을 제공.

## 📝 구체적 구현 방향
1. `InviteShareScreen` 구현 (생성된 약속 요약 카드, 딥링크 복사 버튼).
2. Android `Intent.ACTION_SEND`를 활용한 공유 팝업 (카카오톡, 문자, 클립보드 복사 연동).

## 🔍 검증 방법
- 공유 버튼 클륵 시 공유 다이얼로그 노출 및 클립보드에 URL 복사 확인.
