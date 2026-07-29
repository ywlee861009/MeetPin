# modern-minimal-design-system

## 🎯 목적 / 배경
`:core:designsystem` 모듈에 MVI 베이스 클래스만 있고 실제 디자인 시스템(테마·색·타이포·공용
컴포넌트)이 없었다. 테마는 `:app`에 흩어져 있었고 `dynamicColor=true`라 Android 12+에서
브랜드 색이 사용자 배경화면 색으로 덮이는 문제도 있었다.

사용자 승인 하에 "모던 미니멀" 방향의 HTML 시안(`design-preview/design-system.html`)을 먼저
만들고, 이를 Android 디자인 시스템으로 이식했다.

핵심 원칙
- 강조색은 **핀 레드 하나**(live/여기). 도착(arrived)은 강조색이 아니라 시맨틱 컬러로 분리.
- 데이터(거리·좌표·초대코드·ETA)는 모노스페이스 + tabular numerals.
- 뉴트럴에 미세한 푸른기(지도 바탕과 조화). `dynamicColor` OFF(브랜드 색 보존).

## 하위 티켓
- `phase1_designsystem-module.md` — 토큰·테마·컴포넌트·프리뷰 구축 및 `:app` 배선 (완료)

## 선행 관계
- 없음. 화면 이관은 후속 티켓 `feature-screens-adopt-designsystem`(예정)에서 진행.

## 상태
- ✅ 완료
