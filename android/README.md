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

---

## 🔑 최초 설정: Google Maps API 키

지도 화면을 정상적으로 띄우려면 Google Maps API 키가 필요합니다.
키는 `local.properties`로 주입되며, 이 파일은 `.gitignore`에 등록되어 있어 커밋되지 않습니다.

1. [Google Cloud Console](https://console.cloud.google.com/)에서 프로젝트를 생성합니다.
2. **APIs & Services ➔ Library**에서 **Maps SDK for Android**를 활성화합니다.
3. **APIs & Services ➔ Credentials**에서 API 키를 발급합니다.
   (권장: Android 앱 제한 — 패키지명 `com.meetpin.app` + 디버그 keystore SHA-1 지문 등록)
4. `android/local.properties`에 아래 한 줄을 추가합니다.

   ```properties
   MAPS_API_KEY=여기에_발급받은_키
   ```

5. 빌드하면 `app/build.gradle.kts`가 이 값을 읽어
   `AndroidManifest.xml`의 `${MAPS_API_KEY}` placeholder를 치환합니다.

> 키를 설정하지 않아도 빌드는 통과합니다(CI·온보딩을 막지 않기 위한 의도). 다만 아래 경고가 출력되고
> 지도가 회색 화면으로 표시됩니다.
> ```
> [MeetPin] local.properties에 MAPS_API_KEY가 없습니다. 지도가 회색 화면으로 표시됩니다.
> ```

---

## 🏗️ 빌드 및 검증 명령

```bash
cd android
./gradlew compileDebugKotlin --continue   # 전 모듈 Kotlin 컴파일 검증
./gradlew :app:assembleDebug              # 디버그 APK 빌드
./gradlew test                            # 단위 테스트
./gradlew :app:installDebug               # 연결된 기기/에뮬레이터에 설치
```

### JDK 버전 (JDK 21 고정)

Gradle 8.11.1은 **JDK 24를 지원하지 않습니다.** JDK 24로 실행하면 컴파일은 통과하지만
단위 테스트 태스크 생성 단계에서 아래처럼 실패합니다.

```
Could not create task ':core:domain:testDebugUnitTest'.
> Could not create an instance of type ...DefaultReportContainer. > Type T not present
```

이를 막기 위해 `gradle/gradle-daemon-jvm.properties`에 `toolchainVersion=21`을 고정해 두었습니다.
(Gradle Daemon JVM criteria — incubating 기능) 따라서 `JAVA_HOME`이 JDK 24를 가리켜도
Gradle 데몬은 JDK 21로 실행됩니다. **로컬에 JDK 21이 설치되어 있어야 합니다.**

```bash
/usr/libexec/java_home -V   # 설치된 JDK 확인 (macOS)
```

AI 티켓(`ai-tickets/`)의 phase를 `done/`으로 옮기기 전에는 반드시 위 명령으로 빌드가 통과하는지
확인합니다. 빌드 검증 없이 완료 처리하지 않습니다.
