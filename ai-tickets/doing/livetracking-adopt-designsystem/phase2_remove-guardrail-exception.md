# phase2: 가드레일 예외 제거

## 목표
phase1 완료 후 `DesignSystemGuardrailTest`의 `pendingMigration` 예외를 비운다.

## 대상
- `android/app/src/test/.../guardrail/DesignSystemGuardrailTest.kt`
  - `pendingMigration`에서 `LiveTrackingScreen.kt`, `ParticipantStatusSheet.kt` 제거

## 검증
- [ ] `./gradlew :app:testDebugUnitTest --tests "*DesignSystemGuardrailTest"` 그린
- [ ] 예외 없이 feature 전체가 규칙 통과
