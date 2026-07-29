# phase1: LiveTracking 화면 DS 이관

## 목표
`LiveTrackingScreen.kt`, `ParticipantStatusSheet.kt`를 DS 컴포넌트로 교체한다.

## 대상/방향
- 초대 링크 공유 `Button` → `MeetPinButton`(leadingIcon 슬롯)
- 디버그 `OutlinedButton` 2개 → `MeetPinSecondaryButton`
- 채팅 입력 `OutlinedTextField` → DS에 `MeetPinTextField` 신설 후 교체 (DS에 아직 없음)
- 지도 말풍선/오프스크린 말풍선 → `MeetPinChatBubble` 재사용 검토
- 참가자 아바타/행 → `MeetPinAvatar` / `ParticipantRow`
- 상단 "실시간 공유 중" 바 → `LiveBadge`
- 약속 장소 핀/참가자 마커 → `PinMarker` / `MeetPinAvatar`

## 주의
- 미커밋 WIP(디버그 모의 코드) 정리 후 착수.
- 지도 마커 내부는 `MapMarkerScope` 컨텍스트라 DS 컴포넌트 재사용 시 제약 확인 필요.

## 검증
- [x] `:feature:tracking`, `:app` 컴파일
- [x] 하드코딩 색/직접 M3 원자 컴포넌트 제거

## 완료 메모
- DS에 `MeetPinTextField` 신설(채팅 입력용) 후 `OutlinedTextField` 교체.
- 초대 버튼→`MeetPinButton`, 디버그 버튼→`MeetPinSecondaryButton`,
  지도/오프스크린 말풍선→`MeetPinChatBubble`, 참가자 마커→`MeetPinAvatar`,
  참가자 시트 카드→`ParticipantRow`, 상단 바→`LiveBadge`+`MeetPinGhostButton`.
- 지도 마커 내부는 `MarkerComposable` 서브컴포지션이지만 `MeetPinTheme`가
  `MaterialTheme`와 `extendedColors`를 함께 provide하므로 토큰 전파 확인됨.
