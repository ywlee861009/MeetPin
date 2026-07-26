# [overview] project-multimodule-setup

## 🎯 목적 및 배경
MeetPin 앱을 확장 가능하고 유지보수가 용이하도록 단일 `:app` 구조에서 계층별 **멀티 모듈 구조(Multi-Module Architecture)**로 전환하고, **Hilt (Dependency Injection)** 환경을 구축합니다.

---

## 📋 하위 티켓 (Phases)
- **`phase1_multimodule-structure.md`**: Gradle 모듈 분리 (`:core:domain`, `:core:data`, `:core:model`, `:core:location`, `:core:network`, `:core:designsystem`, `:feature:map`, `:feature:lobby`, `:feature:tracking`) 및 의존성 관계 설정
- **`phase2_hilt-di-setup.md`**: Hilt DI 설치 및 프로젝트 공통 의존성 주입 구조 세팅

---

## 🔗 선행 관계
- 선행 작업 없음 (최우선 진행 작업)
