# Phase 1: designsystem ViewModel 의존성 수정

> **상태: 완료 (2026-07-26)**
>
> - `libs.versions.toml`: `androidx-lifecycle-viewmodel-ktx` 외 lifecycle compose 별칭 추가
> - `core/designsystem/build.gradle.kts`: `api(lifecycle-viewmodel-ktx)`, `api(kotlinx-coroutines-android)` 추가
> - `LiveTrackingViewModel.kt`: `kotlinx.coroutines.launch`/`delay` import 추가, 미사용 import(`PinLocation`, `onEach`) 제거
> - 검증: `./gradlew compileDebugKotlin --continue` → 라이브러리 모듈 9개 전부 성공, `e:` 오류 0건
> - 남은 사항: `:app:compileDebugKotlin`은 `:app:processDebugMainManifest` 실패로 아직 미실행 (phase2 범위)

## 🎯 목표
- `:core:designsystem`의 `BaseViewModel`이 컴파일되도록 의존성을 보정한다.
- `BaseViewModel`을 상속하는 feature 모듈들이 `ViewModel` 타입을 볼 수 있도록 의존성을 `api`로 노출한다.
- `./gradlew compileDebugKotlin --continue`에서 Kotlin 컴파일 오류 0건을 달성한다.

## 🛠️ 수정 대상
- `android/core/designsystem/build.gradle.kts`
- `android/gradle/libs.versions.toml` (`lifecycle-viewmodel-ktx` 별칭이 없으면 추가)
- 컴파일 과정에서 드러나는 feature 모듈의 누락 import
  - 확인된 것: `feature/tracking/.../LiveTrackingViewModel.kt` — `viewModelScope.launch` 사용부에
    `kotlinx.coroutines.launch` import 누락

## 📝 구체적인 구현 방향
- 현재 오류
  ```
  BaseViewModel.kt:3:27 Unresolved reference 'ViewModel'
  BaseViewModel.kt:4:27 Unresolved reference 'viewModelScope'
  ```
  원인은 `build.gradle.kts`에 `androidx.lifecycle.runtime.ktx`만 선언되어 있고
  `lifecycle-viewmodel-ktx`가 없기 때문이다.
- `libs.versions.toml`에 `androidx-lifecycle-viewmodel-ktx` 별칭을 추가하고
  `:core:designsystem`에 `api(libs.androidx.lifecycle.viewmodel.ktx)`로 선언한다.
- `implementation`이 아니라 `api`를 쓰는 이유: `BaseViewModel`이 `ViewModel`을 상속하는 public
  슈퍼타입이므로, 이를 상속하는 feature 모듈의 컴파일 클래스패스에도 `ViewModel`이 노출되어야 한다.
  Compose 관련 의존성도 `BaseViewModel`/공용 컴포넌트 시그니처에 노출되는 범위는 동일 기준으로 검토한다.
- designsystem이 통과한 뒤 하위 모듈에서 새로 드러나는 컴파일 오류를 순차 해결한다
  (지금은 designsystem이 먼저 실패해 하위 오류가 가려져 있음).

## 🔍 검증 방법
```bash
./gradlew :core:designsystem:compileDebugKotlin
./gradlew compileDebugKotlin --continue
```
- 두 명령 모두 `BUILD SUCCESSFUL`이어야 한다.
- `e:` 로 시작하는 Kotlin 컴파일 오류가 0건이어야 한다.
- 이 시점에 `:app:assembleDebug`는 여전히 실패할 수 있다 (Manifest placeholder는 phase2,
  Hilt 바인딩은 phase3 범위).
