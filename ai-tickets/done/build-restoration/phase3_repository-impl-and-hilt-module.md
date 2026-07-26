# Phase 3: Repository 구현체 및 Hilt 모듈 작성

> **상태: 완료 (2026-07-26)** — `:app:assembleDebug` 프로젝트 최초 성공
>
> 작업 내용:
> - `core/data`: `FakeMeetPinRepository`(인메모리, `@Singleton`) + `di/DataModule`(`@Binds`) 신규
> - `core/location`: `di/LocationModule`(`@Provides`) 신규 —
>   `FusedLocationProviderClient`, `LocationClient`, `LocationRepository`, `ArrivalDetector` 제공
> - `core/data`, `core/location` `build.gradle.kts`: Hilt 플러그인 + KSP + coroutines 추가
>
> 계획과 다르게 처리한 부분:
> - `DefaultLocationRepository`/`ArrivalDetector`에 `@Inject constructor`를 붙이는 대신
>   `LocationModule`의 `@Provides`로 생성했습니다. 이 클래스들을 Dagger 비의존 순수 클래스로 유지해
>   phase4의 단위 테스트에서 직접 생성할 수 있게 하려는 의도입니다.
>
> 계획에 없었으나 빌드 차단 요인이라 함께 처리한 부분:
> - **런처 아이콘 리소스 전무** — Manifest가 `@mipmap/ic_launcher`를 참조하는데 리소스가 없어
>   `:app:processDebugResources` 실패. minSdk 28이므로 XML 어댑티브 아이콘으로 해결
>   (`drawable/ic_launcher_foreground.xml`, `mipmap-anydpi-v26/ic_launcher{,_round}.xml`,
>   `colors.xml`에 `ic_launcher_background` 추가)
> - **`MainActivity.kt:22` `super.onCreate()`** 인자 누락 컴파일 오류 → `super.onCreate(savedInstanceState)`
> - **`DefaultLocationRepository.observeGroupLocations`가 영구히 아무 값도 방출하지 않던 문제** —
>   `MutableSharedFlow(replay=1)`에 emit이 없어 `LiveTrackingViewModel`의 `combine`이 첫 emission을
>   못 받고 무한 로딩에 빠짐. `onStart`로 빈 목록 초깃값을 방출하도록 수정
>
> 검증 결과:
> - `./gradlew :app:assembleDebug` → **BUILD SUCCESSFUL**, `app/build/outputs/apk/debug/app-debug.apk` 생성
> - `./gradlew :app:installDebug` → 에뮬레이터(Medium_Phone_API_35) 설치 성공
> - `adb shell am start` → COLD 시작 623ms, `FATAL EXCEPTION` 0건, 프로세스 정상 유지
> - 런처 아이콘 정상 렌더링 확인 (스크린샷)
>
> **미검증/후속 필요 (→ phase5로 분리):**
> - `MainActivity`가 AGP 템플릿 `Greeting("MeetPin")`을 그대로 표시합니다. 프로젝트 전체에
>   `NavHost`/`rememberNavController`가 **하나도 없어** 구현된 feature 화면
>   (`MapScreen`, `CreatePinScreen`, `LobbyScreen`, `InviteAcceptScreen`, `LiveTrackingScreen`,
>   `CompletionScreen`)에 도달할 수 없습니다.
> - 따라서 "핀 생성 → 초대 → 대기실 → 트래킹 플로우 수동 확인"은 수행 불가였습니다.
>   Hilt 그래프는 컴파일 시점으로만 검증되었고, ViewModel 실제 주입은 런타임 미검증입니다.

## 🎯 목표
- `MeetPinRepository` 구현체를 `:core:data`에 작성하고 Hilt로 바인딩한다.
- `LocationRepository` 구현체(`DefaultLocationRepository`)도 Hilt 그래프에 연결한다.
- `./gradlew :app:assembleDebug`를 **프로젝트 최초로** 성공시킨다.

## 🛠️ 수정 대상
- `android/core/data/` (현재 `build.gradle.kts`만 존재하는 빈 모듈)
  - `FakeMeetPinRepository.kt` (신규)
  - `di/DataModule.kt` (신규)
- `android/core/location/`
  - `DefaultLocationRepository.kt` (`@Inject constructor` 부여)
  - `di/LocationModule.kt` (신규)
- `android/core/data/build.gradle.kts`, `android/core/location/build.gradle.kts` (Hilt 플러그인/KSP 추가)
- `android/app/build.gradle.kts` (`:core:data` 의존성 확인)

## 📝 구체적인 구현 방향
- 현재 `MeetPinRepository`는 4개 ViewModel이 주입받고 있으나 구현체가 0개, `@Module`도 0개다.
  ```
  LobbyViewModel, InviteAcceptViewModel, CreatePinViewModel, LiveTrackingViewModel
  ```
  Hilt는 인터페이스 바인딩을 찾지 못해 `:app:kspDebugKotlin`에서 실패한다.
- 서버 API가 아직 없으므로 `:core:network`는 이 phase에서 손대지 않고, `:core:data`에
  인메모리 `FakeMeetPinRepository`를 작성한다.
  - `MutableStateFlow<Map<String, MeetPinGroup>>`로 그룹 상태 보관
  - `createGroup`: 초대 코드 생성 후 그룹 등록, `Result.success` 반환
  - `getGroupByInviteCode`: 미존재 시 `Result.failure`
  - `updateInviteStatus`: 해당 참가자의 `InviteStatus` 갱신 및 전원 승낙 시 `GroupStatus` 전이
  - `observeGroup`: `StateFlow`에서 해당 groupId만 `map` + `filterNotNull`
  - **주의**: 프로덕션 경로에 테스트 데이터를 심는 것이 아니라, 구현체 자체가 Fake임을 클래스명과
    KDoc으로 명시한다. 실제 API 연동 시 `:core:data`의 바인딩만 교체하면 되도록 한다.
- `@Singleton`으로 스코프를 잡아야 화면 간 그룹 상태가 유지된다.
- `DataModule`은 `@Binds`로 인터페이스 → 구현체를 연결한다 (`@InstallIn(SingletonComponent::class)`).
- `DefaultLocationRepository`는 현재 어노테이션 없는 순수 클래스이므로 `@Inject constructor`를
  부여하고 `LocationModule`에서 바인딩한다. `LocationClient`/`ArrivalDetector`의 바인딩도 함께 확인한다.
- 각 모듈 `build.gradle.kts`에 `hilt.android` 플러그인과 `ksp(libs.hilt.compiler)`를 추가한다.

## 🔍 검증 방법
```bash
./gradlew :app:assembleDebug
```
- `BUILD SUCCESSFUL`이어야 한다 (프로젝트 최초 성공 지점).
- 에뮬레이터/실기기에 설치해 앱이 크래시 없이 실행되고, 맵 화면이 뜨는지 확인한다.
- 핀 생성 → 초대 → 대기실 → 트래킹 플로우가 Fake Repository 기반으로 이어지는지 수동 확인한다.
