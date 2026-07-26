# [overview] feature-pin-creation-and-map

## 🎯 목적 및 배경
주선자가 구글 지도 상에서 모임 장소를 검색하거나 지도를 직접 클릭하여 약속 핀(Pin)을 생성하고, 생성된 약속의 딥링크 초대장을 친구들에게 공유하는 UI 및 MVI 흐름을 개발합니다.

---

## 📋 하위 티켓 (Phases)
- **`phase1_google-maps-integration.md`**: Google Maps Compose 뷰 배치 및 사용자 현재 위치 표시
- **`phase2_pin-creation-ui.md`**: 지도 터치 핀 드롭 & 약속 정보(이름, 날짜/시간) 입력 BottomSheet MVI 화면 구현
- **`phase3_deeplink-sharing.md`**: 약속 생성 후 딥링크 발급 및 공유(카카오톡/문자) 뷰 완성

---

## 🔗 선행 관계
- `core-domain-and-model` 완료 후 진행
