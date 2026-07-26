# Phase 4: 모임 종료 및 앨범 저장

## 🎯 목표
- 전원 도착하여 모임이 종료될 때, 해당 인증샷과 참석자 정보를 로컬 DB 또는 기기 갤러리에 추억으로 저장합니다.

## 🛠️ 수정 대상
- `LiveTrackingViewModel.kt` (종료 로직)
- `MemoryHistoryRepository.kt` (신규 DB 또는 DataStore)

## 📝 구체적인 구현 방향
- 모임이 종료 상태(`GroupStatus.FINISHED`)로 전환될 때, 뷰모델에서 앨범 저장 Repository 호출
- 로컬 DB(Room)에 `(모임명, 날짜, 참가자 목록, 인증샷 URI)` 세트를 기록
- 이후 '나의 모임 기록' 화면에서 불러올 수 있도록 데이터 기반 마련

## 🔍 검증 방법
- 모임 종료 이벤트 발생 시 에러 없이 DB에 데이터가 저장되는지 로그 및 디버깅 확인
