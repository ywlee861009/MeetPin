# phase5 (선택): 데모 그룹으로 바로 진입하는 디버그 단축

## 문제 / 목표
UI를 반복해서 보려면 매번 "핀 생성 → 대기실 → 승낙 → 트래킹"을 거쳐야 한다.
데모/디버그 시 이 과정을 건너뛰고 **트래킹 화면으로 즉시 진입**하는 단축을 둔다.
(순수 개발 편의 기능 — 프로덕션 비노출.)

## 구현 방향 (택1)
1. **디버그 진입 버튼**: 첫 화면(지도/홈)에 `showDebugTools`일 때만 보이는
   "🧪 데모 트래킹 열기" 버튼. 눌리면 Fake에 데모 그룹을 생성(전원 ACCEPTED로)하고
   해당 `groupId`로 트래킹 목적지로 네비게이트.
   - `FakeMeetPinRepository`에 `createDemoGroup()`(호스트+친구2, 전원 ACCEPTED, ACTIVE) 추가.
2. **디버그 딥링크**: `meetpin://demo` 같은 내부 딥링크로 동일 동작. adb로 실행 가능:
   `adb shell am start -a android.intent.action.VIEW -d "meetpin://demo"`.

## 주의
- 네비게이션 그래프(`feature`/`app`의 NavHost) 라우트와 인자 규약을 따를 것.
  `post-navigation-gaps`에서 백스택/`launchSingleTop` 관련 수정이 있었으므로 중복 목적지
  누적이 없는지 확인.
- 디버그 전용 코드는 `showDebugTools`/BuildConfig.DEBUG로 게이팅, DS 컴포넌트만 사용.

## 검증
- [ ] 디버그 진입 시 핀 생성 없이 트래킹 화면에 3명 참가자가 보임
- [ ] 뒤로가기 시 백스택이 정상(트래킹 중복 누적 없음)
- [ ] release 빌드에는 노출되지 않음
