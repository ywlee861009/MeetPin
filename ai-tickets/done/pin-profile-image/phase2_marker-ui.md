# Phase 2: 마커 UI 개선 (Coil 연동)

## 🎯 목표
- 지도 위의 핀 마커(`AvatarMarker`)에 실제 프로필 이미지를 렌더링합니다.

## 🛠️ 수정 대상
- `LiveTrackingScreen.kt` (`AvatarMarker` 컴포저블)
- `build.gradle` (필요시 Coil 라이브러리 추가)

## 📝 구체적인 구현 방향
- `AvatarMarker`에서 `profileImageUrl`을 매개변수로 받도록 수정합니다.
- `Coil`의 `AsyncImage`를 사용하여 이미지를 비동기로 불러와 원형(CircleShape)으로 렌더링합니다.
- 이미지가 없거나 로드에 실패할 경우 기존의 이니셜 폴백(Fallback) UI를 보여주도록 에러 처리를 추가합니다.

## 🔍 검증 방법
- 지도에 유저 마커가 이미지로 표시되는지 확인
- 네트워크 지연이나 이미지 오류 시 이니셜이 정상 출력되는지 확인
- 마커 이동 애니메이션 등 지도 렌더링 성능에 저하가 없는지 확인
