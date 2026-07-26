# 📱 MeetPin Android 개발 지침 (Guidelines)

MeetPin Android 프로젝트는 지속 가능하고 확장성 있는 코드베이스를 위해 아래의 4가지 핵심 원칙을 반드시 준수합니다.

---

## 📌 핵심 개발 원칙 (Core Principles)

### 1. Jetpack Compose 사용 (UI Framework)
- 모든 UI는 **Jetpack Compose** 선언형 프레임워크로 작성합니다.
- XML 기반의 Legacy View 레이아웃은 사용하지 않으며, Compose 가이드라인(State Hoisting, Recomposition 최적화)을 준수합니다.

### 2. MVI 아키텍처 패턴 (Model-View-Intent)
- UI 상태 관리 및 이벤트 처리는 **MVI 패턴**을 적용합니다.
  - **State**: UI의 모든 상태를 하나의 Immutable `UiState` 객체로 정의합니다.
  - **Intent / Event**: 사용자의 입력 및 이벤트를 `UiIntent` (또는 `UiEvent`)로 처리하여 ViewModel로 전달합니다.
  - **Side Effect**: 일회성 이벤트(Toast, 화면 이동 등)는 `Channel` / `SharedFlow` 기반의 `UiEffect`로 구독 처리합니다.
- 단방향 데이터 흐름(Unidirectional Data Flow, UDF)을 엄격히 지킵니다.

### 3. 멀티 모듈 구성 (Multi-Module Architecture)
- 단일 `:app` 모듈에 모든 코드를 넣지 않고, 기능 및 관심사별 **멀티 모듈**로 계층을 분리합니다.
  - `:app`: 의존성 주입(Hilt) 및 전체 네비게이션/엔트리 포인트
  - `:feature:*`: 기능 단위 UI 모듈 (`:feature:map`, `:feature:lobby`, `:feature:tracking` 등)
  - `:core:domain`: 순수 비즈니스 로직 및 UseCase (Android SDK 독립적)
  - `:core:data`: Repository 구현체, 로컬/원격 데이터소스
  - `:core:location`: FusedLocationProvider 및 Foreground Service 위치 추적 엔진
  - `:core:network`: WebSocket 및 API 통신 모듈
  - `:core:designsystem`: 재사용 가능한 Compose 공통 컴포넌트 및 테마

### 4. 명확한 관심사 분리 (Strict Separation of Concerns)
- Presentation, Domain, Data, Infrastructure 계층 간 인터페이스를 명확히 정의합니다.
- 상위 계층은 하위 계층의 구체 구현체에 의존하지 않고 인터페이스에 의존합니다 (의존성 역전 원칙 - DIP).
- 위치 정보 수집, 네트워크 소켓 통신, UI 렌더링 로직이 섞이지 않도록 모듈 및 클래스 레벨에서 철저히 분리합니다.
