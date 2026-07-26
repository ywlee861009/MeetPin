# [TICKET-001] Pin에 프로필 사진 설정 기능

## 🎯 목표
- 실시간 트래킹 화면(`LiveTrackingScreen`)의 유저 마커(Pin)에 닉네임 첫 글자 대신 실제 유저의 프로필 사진이 표시되도록 구현합니다.

## 📝 세부 작업 목록
- [ ] `Participant` 및 관련 Data Model에 `profileImageUrl` 필드 추가
- [ ] `LiveTrackingScreen.kt` 내의 `AvatarMarker` 컴포저블 수정
- [ ] `Coil` 라이브러리의 `AsyncImage` 등을 사용하여 프로필 이미지 비동기 로딩 구현
- [ ] 이미지가 없는 유저나 로딩 실패 시 기존처럼 닉네임 이니셜 폴백(Fallback) UI 유지
- [ ] 마커가 원형(CircleShape)을 유지하고 테두리(Border)가 이쁘게 표시되도록 UI 디자인 개선

## 🔍 검증 기준
- [ ] 맵 상의 유저 마커에 프로필 사진이 둥근 형태로 잘 표시되는가?
- [ ] 프로필 이미지가 없는 경우 이니셜이 정상적으로 표시되는가?
- [ ] 빌드 성공 및 맵 렌더링 시 성능 저하가 없는가?
