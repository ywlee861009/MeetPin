# Phase 4: Mock 주입 제거 및 도메인 로직 분리 + 단위 테스트 도입

> **상태: 완료 (2026-07-26)**
>
> 작업 내용:
> - `LiveTrackingViewModel`: `[Mock Data Injection for Testing]` 블록 제거
>   (`scheduledAt`/`penaltyPerMinute` 덮어쓰기 삭제 → 실제 그룹 데이터 사용)
> - 타이머 1초 → **60초**(`TICK_INTERVAL_MS`)로 변경, `minuteTicker()`로 추출.
>   매직넘버 `4000`도 `CHAT_BUBBLE_DURATION_MS` 상수로 정리
> - `:core:domain/usecase/` 신규: `CalculateEtaUseCase`, `CalculateLatePenaltyUseCase`(+`LatePenalty`)
>   — 현재 시각을 파라미터로 주입받는 순수 함수. ViewModel은 호출만 담당
> - `:core:domain`: Hilt + KSP + JUnit 추가, `:core:model`을 `api`로 전파
> - `:core:domain/src/test/` 신규 — **프로젝트 최초의 단위 테스트 소스셋**
>
> 계획에 없었으나 함께 처리한 부분:
> - **JDK 24 + Gradle 8.11.1 비호환 발견.** 테스트 태스크 생성 자체가
>   `Could not create task ':core:domain:testDebugUnitTest' ... Type T not present`로 실패했습니다.
>   `gradle/gradle-daemon-jvm.properties`에 `toolchainVersion=21`을 고정(Gradle Daemon JVM criteria)해
>   `JAVA_HOME`이 JDK 24여도 데몬이 JDK 21로 실행되도록 했습니다. README에 증상과 함께 기록했습니다.
>
> 계획에서 제외한 부분:
> - `ArrivalDetector.calculateDistance` 테스트는 작성하지 않았습니다.
>   내부적으로 Android SDK의 `Location.distanceBetween`(정적 native 계열)을 호출하므로
>   JVM 단위 테스트에서 "not mocked" 예외가 발생합니다. Robolectric 도입 또는 `androidTest`로
>   옮기는 것이 맞아 이 phase 범위에서 제외했습니다.
>
> 검증 결과:
> - `./gradlew :core:domain:testDebugUnitTest --rerun-tasks` → **BUILD SUCCESSFUL**
>   - `CalculateEtaUseCaseTest` 8건, `CalculateLatePenaltyUseCaseTest` 8건 = **16건 전부 통과**
>     (failures=0, errors=0, skipped=0)
> - `./gradlew test :app:assembleDebug` → BUILD SUCCESSFUL
> - `grep`으로 `Mock Data Injection` / `penaltyPerMinute = 1000` / `delay(1000)` 잔존 0건 확인
> - 에뮬레이터 재설치 후 실행 → COLD 725ms, `FATAL EXCEPTION` 0건
> - 지각 표시가 실기기에서 분 단위로 증가하는지는 **미검증** — 트래킹 화면에 도달할 수 없기 때문입니다
>   (네비게이션 미구현, phase5 참조). 대신 계산 로직은 위 단위 테스트로 검증했습니다.

## 🎯 목표
- 프로덕션 경로에 박힌 테스트용 Mock 데이터 주입을 제거한다.
- 1초 주기 타이머를 실제 요구사항(분 단위)에 맞게 정리한다.
- ETA / 지각 시간 / 페널티 계산을 `:core:domain`의 순수 함수(UseCase)로 분리한다.
- 프로젝트 최초의 단위 테스트를 도입하여 이후 검증 수단을 확보한다.

## 🛠️ 수정 대상
- `android/feature/tracking/src/main/java/.../LiveTrackingViewModel.kt`
- `android/core/domain/src/main/java/.../usecase/` (신규)
  - `CalculateEtaUseCase.kt`
  - `CalculateLatePenaltyUseCase.kt`
- `android/core/domain/src/test/java/.../usecase/` (신규 테스트 소스셋)
- `android/core/domain/build.gradle.kts` (JUnit / kotlin-test 의존성 추가)
- `android/gradle/libs.versions.toml` (테스트 라이브러리 별칭)

## 📝 구체적인 구현 방향
### 1. Mock 주입 제거
`LiveTrackingViewModel.kt:56-61`에 다음 코드가 프로덕션 경로에 남아 있다.
```kotlin
// [Mock Data Injection for Testing]
val group = groupData.copy(
    scheduledAt = System.currentTimeMillis() - (3 * 60 * 1000),
    penaltyPerMinute = 1000
)
```
매 emission마다 실제 약속 시간과 벌금 설정을 덮어쓴다. 제거하고 `groupData`를 그대로 사용한다.
지각비 동작 확인은 phase4에서 추가하는 단위 테스트로 대체한다.

### 2. 타이머 정리
```kotlin
flow { while (true) { emit(System.currentTimeMillis()); delay(1000) } }
```
1초마다 전체 참가자 마커 리스트를 재생성하고 있다. 지각 표시는 분 단위이므로
- 주기를 60초로 변경
- 또는 `lateMinutes`가 바뀔 때만 하위 스트림이 흐르도록 `distinctUntilChanged` 적용
`combine`의 재계산 비용과 Compose 리컴포지션을 함께 줄인다.

### 3. 도메인 로직 분리
현재 ViewModel 안에 인라인으로 있는 계산을 `:core:domain`으로 옮긴다.
CLAUDE.md의 관심사 분리(SoC) 원칙에 맞추고, 테스트 가능한 순수 함수로 만든다.
- `CalculateEtaUseCase(distanceMeters: Float, walkingSpeedKmh: Float = 5f): Int?`
- `CalculateLatePenaltyUseCase(scheduledAt: Long, now: Long, isArrived: Boolean, penaltyPerMinute: Int): LatePenalty`
  - `LatePenalty(lateMinutes: Int, amount: Int)`
- `System.currentTimeMillis()`를 UseCase 내부에서 호출하지 않고 파라미터로 주입해 테스트 가능하게 한다.
- ViewModel은 이 UseCase를 주입받아 호출만 담당한다.

### 4. 단위 테스트 도입
`:core:domain`에 `src/test` 소스셋을 만들고 다음 케이스를 작성한다.
- ETA: 거리 0 → null, 1.39m/s 기준 거리별 분 환산, 최소 1분 보정
- 페널티: 약속 시간 이전 → 0, 도착한 참가자 → 0, 경과 분 × 단가, 경계값(정확히 약속 시간)
- `ArrivalDetector.calculateDistance`의 알려진 좌표쌍 거리 검증

## 🔍 검증 방법
```bash
./gradlew :core:domain:testDebugUnitTest
./gradlew :app:assembleDebug
```
- 단위 테스트 전부 통과, `assembleDebug` 성공.
- 코드 전체에 `Mock Data Injection` 주석과 하드코딩된 `penaltyPerMinute = 1000`이 남아 있지 않은지
  grep으로 확인한다.
- 실기기에서 실제 약속 시간을 과거로 설정한 그룹에서 지각 표시가 분 단위로 증가하는지 확인한다.
