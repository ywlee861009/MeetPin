# phase2: mvi-base-framework

## 🎯 문제 및 목표
- 모든 Feature 모듈에서 공통으로 적용할 MVI (Model-View-Intent) 베이스 계약 및 ViewModel 클래스 구현.

## 📝 구체적 구현 방향
1. `:core:designsystem` 또는 `:core:common` 내 MVI 계약 인터페이스 정의:
   - `UiState`: UI 불변 상태를 나타내는 마커 인터페이스
   - `UiIntent`: 사용자 액션을 나타내는 마커 인터페이스
   - `UiEffect`: 일회성 이벤트(Side Effect)를 나타내는 마커 인터페이스
2. `BaseViewModel<STATE : UiState, INTENT : UiIntent, EFFECT : UiEffect>` 구현:
   - `StateFlow<STATE>`를 통한 상태 노출
   - `Channel<EFFECT>`를 통한 일회성 부수효과 노출
   - `processIntent(intent: INTENT)` 추상 메서드 제공

## 🔍 검증 방법
- 샘플 ViewModel 구현 및 State / Effect 동작 단위 테스트 작성.
