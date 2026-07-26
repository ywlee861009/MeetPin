# phase1: multimodule-structure

## 🎯 문제 및 목표
- 단일 `:app` 모듈에 모여있는 구성을 관심사별 모듈로 분리하여 빌드 속도 향상 및 계층 간 결합도를 낮춤.

## 🛠️ 대상 모듈 및 구성
```
android/
├── app/
├── core/
│   ├── data/
│   ├── designsystem/
│   ├── domain/
│   ├── location/
│   ├── model/
│   └── network/
└── feature/
    ├── lobby/
    ├── map/
    └── tracking/
```

## 📝 구체적 구현 방향
1. `settings.gradle.kts`에 신규 모듈 `include` 등록.
2. `:core:model` & `:core:domain` 모듈 생성 (Android/Java Library).
3. `:core:designsystem`, `:core:data`, `:core:location`, `:core:network` Android Library 모듈 생성.
4. `:feature:map`, `:feature:lobby`, `:feature:tracking` Android Library 모듈 생성.
5. 모듈 간 의존성 그래프 연결 (`feature` ➔ `core`, `app` ➔ `feature` & `core`).

## 🔍 검증 방법
- `./gradlew projects` 및 `./gradlew assembleDebug` 정상 동작 여부 확인.
