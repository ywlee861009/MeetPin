# phase2: 가드레일 예외 제거

## 목표
phase1 완료 후 `DesignSystemGuardrailTest`의 `pendingMigration` 예외를 비운다.

## 대상
- `android/app/src/test/.../guardrail/DesignSystemGuardrailTest.kt`
  - `pendingMigration`에서 `LiveTrackingScreen.kt`, `ParticipantStatusSheet.kt` 제거

## 검증
- [x] `./gradlew :app:testDebugUnitTest --tests "*DesignSystemGuardrailTest"` 그린
- [x] 예외 없이 feature 전체가 규칙 통과

## 완료 메모
- `pendingMigration`을 `emptySet()`으로 비움. 규칙 2가 모든 feature 화면에 전면 적용.
