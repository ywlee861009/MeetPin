# [overview] core-domain-and-model

## 🎯 목적 및 배경
MeetPin 비즈니스 핵심 엔티티 객체(`MeetPinGroup`, `PinLocation`, `Participant`, `LocationUpdate`)를 정의하고, MVI 패턴에서 사용할 공통 아키텍처 베이스 클래스(`UiState`, `UiIntent`, `UiEffect`, `BaseViewModel`)를 구축합니다.

---

## 📋 진행 상태 (Status)
| Phase | 내용 | 상태 |
| :--- | :--- | :--- |
| **`phase1_domain-entities.md`** | 도메인 모델 및 Repository 인터페이스 정의 | ✅ **완료 (Done)** |
| **`phase2_mvi-base-framework.md`** | Compose & StateFlow 기반 MVI 베이스 아키텍처 작성 | ✅ **완료 (Done)** |

---

## 🔗 선행 관계
- `project-multimodule-setup` 완료 후 진행
