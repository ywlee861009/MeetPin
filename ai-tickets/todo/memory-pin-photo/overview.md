# 도착 인증샷 (Memory Pin) (memory-pin-photo)

## 🎯 목적과 배경
약속 장소에 도착한 사용자가 현장 사진을 찍어 목적지 핀에 남길 수 있는 기능입니다. 약속이 끝난 후 이 사진을 모임의 추억(앨범)으로 간직할 수 있어 앱의 리텐션과 사용자 만족도를 높입니다.

## 📝 하위 티켓 목록
- `phase1_camera-permission-and-capture.md`: 카메라 권한 요청 및 사진 촬영 런처 연동
- `phase2_photo-upload-state.md`: 촬영된 이미지 상태 관리 및 Mock 업로드 처리
- `phase3_map-pin-photo-ui.md`: 약속 장소(목적지 핀) 위에 인증샷 썸네일 렌더링
- `phase4_album-save.md`: 모임 종료 시 인증샷을 로컬 앨범(또는 히스토리 DB)에 저장하는 로직

## 🔄 선행 관계
- phase1 ➔ phase2 ➔ phase3 ➔ phase4 순으로 진행
