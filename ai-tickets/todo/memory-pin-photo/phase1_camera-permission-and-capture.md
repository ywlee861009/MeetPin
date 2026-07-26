# Phase 1: 카메라 권한 및 촬영 기능

## 🎯 목표
- 도착 상태가 된 사용자에게 "인증샷 찍기" 버튼을 제공하고, 카메라 권한 획득 후 사진을 촬영합니다.

## 🛠️ 수정 대상
- `LiveTrackingScreen.kt` (도착 시 나타나는 바텀 시트 또는 다이얼로그)
- `AndroidManifest.xml` (카메라 권한 추가)

## 📝 구체적인 구현 방향
- `isArrived == true`일 때 하단 패널에 "도착 인증샷 남기기" 버튼을 노출
- `rememberLauncherForActivityResult`를 활용하여 `TakePicturePreview` 또는 `TakePicture` 인텐트 호출
- Android 버전에 따른 카메라 권한(`CAMERA`) 요청 로직 구현

## 🔍 검증 방법
- 목적지 반경 진입(도착) 시 촬영 버튼이 활성화되는지 확인
- 버튼 클릭 시 권한을 묻고 카메라 앱으로 정상 진입하는지 확인
