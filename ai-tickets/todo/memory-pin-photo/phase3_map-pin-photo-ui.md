# Phase 3: 약속 장소 핀 인증샷 UI 렌더링

## 🎯 목표
- 촬영/업로드된 인증샷을 맵 중앙의 약속 장소 핀(목적지 핀) 위에 폴라로이드 사진처럼 예쁘게 띄워줍니다.

## 🛠️ 수정 대상
- `LiveTrackingScreen.kt` (목적지 Marker)

## 📝 구체적인 구현 방향
- 기본 Google Map `Marker` 대신 `MarkerComposable`을 사용하여 목적지 핀 UI 커스텀
- `pinLocation.memoryPhotoUrl` 상태가 존재하면 하얀 테두리를 가진 이미지(Coil `AsyncImage` 활용)를 렌더링
- 사진 클릭 시 크게 보는 다이얼로그 뷰 추가 (선택 사항)

## 🔍 검증 방법
- 사진 등록 후 맵의 목적지 핀 모양이 사진 썸네일로 변경되는지 확인
