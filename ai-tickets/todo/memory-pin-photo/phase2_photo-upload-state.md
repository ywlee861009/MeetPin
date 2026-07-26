# Phase 2: 이미지 상태 관리 및 업로드 처리

## 🎯 목표
- 촬영된 이미지(Bitmap 또는 Uri)를 ViewModel 상태로 가져오고 핀 데이터에 연동합니다.

## 🛠️ 수정 대상
- `LiveTrackingViewModel.kt`
- `LiveTrackingContract.kt`

## 📝 구체적인 구현 방향
- `LiveTrackingState`에 `capturedPhotoUri: Uri?` (또는 Bitmap) 상태 추가
- 사진 촬영 완료 시 Intent를 통해 ViewModel로 전달 (`SaveMemoryPhoto(uri)`)
- 서버 연동 전이므로 로컬 상태의 `PinLocation` 모델에 `memoryPhotoUrl` 필드를 임시로 할당하는 Mock 로직 추가

## 🔍 검증 방법
- 사진 촬영 후 뷰모델 상태에 이미지 데이터가 정상적으로 저장되는지 확인
