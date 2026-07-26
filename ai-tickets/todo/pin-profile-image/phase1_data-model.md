# Phase 1: 데이터 모델 업데이트

## 🎯 목표
- 유저 데이터 모델에 프로필 이미지 정보를 추가하여 UI에서 사용할 수 있도록 준비합니다.

## 🛠️ 수정 대상
- `Participant` (또는 관련 도메인/데이터 모델 파일)
- 데이터 소스 (Mock 데이터 또는 API 응답 모델)

## 📝 구체적인 구현 방향
- `Participant` 데이터 클래스에 `profileImageUrl: String? = null` 속성을 추가합니다.
- 초기 로딩 시 이미지 URL을 주입받을 수 있도록 Mock 데이터나 백엔드 연동 로직을 업데이트합니다.

## 🔍 검증 방법
- 앱 빌드 성공 여부
- UI 상태(`uiState`)에서 `profileImageUrl` 값 접근 가능 여부
