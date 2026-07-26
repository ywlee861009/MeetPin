# phase2: hilt-di-setup

## 🎯 문제 및 목표
- Hilt (Dagger Hilt) 플러그인 및 의존성을 프로젝트 전반에 설정하여 멀티 모듈 및 Compose 환경에서 자동 의존성 주입 지원.

## 📝 구체적 구현 방향
1. `gradle/libs.versions.toml`에 Hilt 버전 (`hilt = "2.51.1"`) 및 플러그인/라이브러리 추가.
2. Root `build.gradle.kts` 및 `app/build.gradle.kts`, `:core:*`, `:feature:*` 모듈에 hilt 플러그인 적용.
3. `@HiltAndroidApp` 적용 (`MeetPinApp.kt`).
4. `@AndroidEntryPoint` 적용 (`MainActivity.kt`).
5. Compose Hilt ViewModel 연동 (`hilt-navigation-compose`) 라이브러리 추가.

## 🔍 검증 방법
- 앱 실행 시 Hilt 초기화 에러 없이 정상 실행 확인.
