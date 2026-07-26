# Phase 1: WindowSizeClass 도입

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
