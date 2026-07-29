# phase1: designsystem 모듈 구축 ✅

## 목표
승인된 모던 미니멀 시안을 `:core:designsystem`에 이식하고 `:app` 테마를 교체한다.

## 수정/생성 대상
### 신규 — `core/designsystem/.../theme/`
- `Color.kt` — 원시 팔레트(핀 레드, arrived 그린, 쿨 뉴트럴, error)
- `ColorScheme.kt` — 라이트/다크 M3 ColorScheme 매핑
- `ExtendedColors.kt` — M3에 없는 시맨틱(ink/arrived/border/codeSurface) + `LocalMeetPinExtendedColors`
- `Type.kt` — Typography 스케일 + 모노 `MeetPinTextStyles`(data/code, tnum)
- `Shape.kt` — Shapes + `PillShape` + `BottomSheetShape`
- `Theme.kt` — `MeetPinTheme` 함수(dynamicColor 제거) + `MeetPinTheme` 접근자 객체

### 신규 — `core/designsystem/.../component/`
- `MeetPinButton.kt` — Primary/Secondary/Ghost
- `MeetPinAvatar.kt` — 이니셜/도착 체크/이미지 슬롯 + `CheckMark`(Canvas)
- `StatusPill.kt` — Moving/Arrived/Sharing
- `LiveBadge.kt` — 실시간 배지 + `PulsingDot`(맥동 애니메이션)
- `InviteCodeBox.kt` — 모노 코드 + 복사 버튼
- `Modifiers.kt` — `dashedBorder`
- `ChatBubble.kt` — 내/상대 말풍선
- `ParticipantRow.kt` — 아바타·이름/상태·거리(모노)
- `MeetPinBottomSheet.kt` — 그랩 핸들 시트 표면
- `PinMarker.kt` — 물방울 핀(Canvas)
- `PreviewGallery.kt` — 라이트/다크 @Preview

### 변경
- `app/.../MainActivity.kt` — theme import를 designsystem으로 변경
- `app/.../ui/theme/*` (Color/Theme/Type) — 삭제
- `core/designsystem/build.gradle.kts` — animation, debug ui-tooling 추가

## 검증
- [x] `:core:designsystem:compileDebugKotlin` 성공
- [x] `:app:compileDebugKotlin`(feature 3종 포함) 성공

## 알려진 트레이드오프
- 핀 레드 위 흰 텍스트 대비는 AA-large 구간 → 버튼 라벨은 항상 볼드(labelLarge)로 사용.
- 한글 폰트는 시스템 고딕 폴백(Pretendard 미임베드). 커스텀 폰트는 `res/font` 추가 후
  `Type.kt`의 `MeetPinFontFamily`만 교체하면 됨.
