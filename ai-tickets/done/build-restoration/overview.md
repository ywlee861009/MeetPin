# 빌드 복구 및 검증 기반 마련 (build-restoration)

## 🎯 목적과 배경
현재 프로젝트는 **컴파일이 되지 않는 상태**입니다. `./gradlew :app:assembleDebug` 및
`./gradlew compileDebugKotlin`이 모두 실패하며, 지금까지 완료 처리된 phase들은 빌드 검증 없이
`done/`으로 이동되었습니다.

확인된 차단 요인:

1. `:core:designsystem`에 `lifecycle-viewmodel-ktx` 의존성이 없어 `BaseViewModel`이 컴파일 불가
   (`ViewModel`, `viewModelScope` unresolved) → MVI 기반 클래스가 깨져 모든 feature 모듈이 연쇄 실패
2. `AndroidManifest.xml`의 `${MAPS_API_KEY}` placeholder가 빌드 스크립트에 배선되지 않음
   → `:app:processDebugMainManifest` 실패
3. `MeetPinRepository` 구현체와 Hilt `@Module`이 프로젝트 전체에 존재하지 않음
   (`:core:data`, `:core:network`는 `build.gradle.kts`만 있는 빈 모듈) → Hilt KSP 실패 예정
4. 프로덕션 코드에 테스트용 Mock 데이터 주입 및 1초 주기 타이머가 잔존
5. 단위 테스트 소스셋(`test`/`androidTest`)이 전무하여 향후 검증 수단이 없음

새 기능 티켓 착수 전에 "빌드가 통과하고, 앱이 실행되며, 로직을 테스트로 검증할 수 있는" 상태를
복구하는 것이 이 티켓의 목표입니다.

## 📝 하위 티켓 목록
| phase | 내용 | 상태 |
|---|---|---|
| `phase1_designsystem-viewmodel-dependency.md` | `:core:designsystem` 의존성 수정으로 전 모듈 Kotlin 컴파일 통과 | ✅ 완료 |
| `phase2_maps-api-key-placeholder.md` | `MAPS_API_KEY` placeholder 배선 및 키 설정 절차 문서화 | ✅ 완료 |
| `phase3_repository-impl-and-hilt-module.md` | `:core:data`에 Repository 구현체 + Hilt 모듈 작성으로 `assembleDebug` 성공 | ✅ 완료 |
| `phase4_cleanup-mock-and-domain-usecase.md` | Mock 주입 제거, 타이머 정리, ETA/페널티 로직 UseCase 분리 및 단위 테스트 도입 | ✅ 완료 |
| `phase5_navigation-wiring.md` | (phase3에서 발견) `NavHost` 배선으로 구현된 화면들을 실제로 도달 가능하게 만들기 | ↪️ `app-navigation` 티켓으로 대체 (SUPERSEDED) |

**이 티켓은 전체 완료되었습니다.** phase1~4는 구현으로 완료했고, phase5는 범위를 3개 phase로
세분화한 `app-navigation` 티켓이 대신 수행해 완료했습니다(시작 목적지와 딥링크 처리 방식이 달라진
경위는 `phase5_navigation-wiring.md` 상단 참고). phase5 문서는 기록 목적으로만 남습니다.

## 🔄 선행 관계
- phase1 ➔ phase2 ➔ phase3 ➔ phase4 순으로 진행
- phase1이 통과하지 않으면 이후 phase의 검증 자체가 불가능하므로 순서 변경 불가
- phase3 완료 시점에 처음으로 `assembleDebug`가 성공해야 함
- phase5는 phase3 진행 중 발견된 항목으로, phase4와 독립적으로 수행 가능

## 📊 현재 달성 상태 (phase1~4 완료 시점)
```
./gradlew compileDebugKotlin --continue   BUILD SUCCESSFUL
./gradlew :app:assembleDebug              BUILD SUCCESSFUL  ← 프로젝트 최초
./gradlew test                            BUILD SUCCESSFUL (16건 통과)
./gradlew :app:installDebug               에뮬레이터 실행 성공, 크래시 0건
```

**이 티켓 범위를 벗어나 남아 있는 것** (각각 별도 티켓에서 다룸)
- `MAPS_API_KEY` 실제 키 미설정 → 지도 회색 표시 (`post-navigation-gaps` phase1)
- `CompletionScreen` 도달 경로 부재 → 도착 판정을 그룹 상태에 반영하는 배선이 없음
  (`post-navigation-gaps` phase4)
- `MeetPinRepository`는 인메모리 Fake이고 `:core:network`는 소스가 없는 빈 모듈
  — 서버 API 연동은 티켓 미작성
- 테스트는 `:core:domain`에만 존재 — ViewModel/UI 테스트는 미도입
- `MapScreen`은 시작 목적지가 `CreatePinScreen`으로 정해지면서 참조되지 않는 상태
  — 삭제 또는 홈 화면 티켓에서의 재활용 여부 미결정
