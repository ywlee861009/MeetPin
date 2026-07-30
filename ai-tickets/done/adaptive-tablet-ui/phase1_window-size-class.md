# Phase 1: WindowSizeClass 도입

> **상태: ✅ 완료 (2026-07-30)**
> - `material3-window-size-class` 의존성 추가 (BOM 버전 관리). 카탈로그 alias는
>   Gradle 예약어 `class` 회피를 위해 `androidx-material3-window-sizeclass`로 명명.
> - `:core:designsystem`에 `LocalWindowSizeClass` CompositionLocal 신설
>   (`window/WindowSizeClass.kt`). feature 모듈이 `:app` 의존 없이 소비하도록 DS에 배치.
>   기본값은 Compact(폰)라 provider 없는 `@Preview`·테스트에서도 크래시 없음.
>   편의 확장 `isExpandedWidth`/`isCompactWidth` 포함.
> - `MainActivity`에서 `calculateWindowSizeClass(this)` 계산 → `LocalWindowSizeClass provides`로 전역 공급.
>   검증용 `Log.d("WindowSizeClass", ...)` 추가(폰/태블릿 인식 확인).
> - 검증: `:core:designsystem`·`:app` `compileDebugKotlin` 성공. 로그 런타임 확인은 에뮬레이터에서 수행.
>
> 참고: designsystem엔 `api`(타입 노출), `:app`엔 `implementation`으로 의존.

## 🎯 목표
- 화면 크기(폰, 폴더블, 태블릿)를 감지하고 상태로 관리할 수 있도록 기반을 다집니다.

## 🛠️ 수정 대상
- `MainActivity.kt`
- `libs.versions.toml` 및 `build.gradle.kts` (material3-window-size-class 종속성 추가)
- 앱의 최상위 테마/네비게이션 래퍼 컴포저블

## 📝 구체적인 구현 방향
- `androidx.compose.material3:material3-window-size-class` 라이브러리를 추가합니다.
- `calculateWindowSizeClass(activity)`를 활용하여 현재 화면의 `WidthSizeClass` (Compact, Medium, Expanded)를 구합니다.
- 계산된 사이즈 클래스를 `CompositionLocalProvider`로 하위 컴포넌트에 공급하거나 앱 전역 상태로 관리합니다.

## 🔍 검증 방법
- 폰 모드와 태블릿 모드(에뮬레이터)에서 각각 다른 사이즈 클래스(Compact vs Expanded)가 정상적으로 인식되는지 로그로 확인
