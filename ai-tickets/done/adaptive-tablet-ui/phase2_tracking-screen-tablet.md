# Phase 2: 실시간 트래킹 화면 태블릿 레이아웃 (Side-by-side)

> **상태: ✅ 완료 (2026-07-30, 런타임 확인 대기)**
> - `LiveTrackingScreen`을 `LocalWindowSizeClass`(phase1)로 분기:
>   - `isExpandedWidth`(태블릿/대화면) → `Row` { 좌측 패널 1/3 + 지도 2/3 }.
>     패널은 위→아래로 헤더 / 참가자 리스트(`weight(1f)`) / 채팅 입력.
>   - 그 외(폰·Medium) → 기존 세로 `Column`(정보 → 지도 2/3 → 채팅 → 참가자 1/3) 유지.
> - 중복·버그 방지를 위해 재사용 컴포저블 추출:
>   - `TrackingMapArea` — 지도 + 마커 + **오프스크린 말풍선 오버레이**(mapSize·cameraState 결합부).
>   - `TrackingHeader` — 라이브바 + 초대버튼 + 디버그 시뮬 버튼.
>   - 헤더/지도/참가자/채팅을 `@Composable` 람다로 hoist해 두 배치가 같은 인스턴스를 조립.
> - 검증: `:feature:tracking:compileDebugKotlin` 성공, `DesignSystemGuardrailTest` 통과.
>   태블릿/폰 에뮬레이터 시각 확인은 미수행(런타임).
>
> 참고: Medium width는 폰 배치로 둔다(Material 가이드 — 단일 pane 허용). Expanded만 분할.

## 🎯 목표
- 실시간 트래킹 화면(`LiveTrackingScreen`)을 태블릿 화면에 맞게 재배치합니다.

## 🛠️ 수정 대상
- `LiveTrackingScreen.kt`

## 📝 구체적인 구현 방향
- `WidthSizeClass`가 `Expanded`(태블릿)인 경우, 세로 배치(`Column`) 대신 가로 배치(`Row`)를 사용합니다.
- 폰(Compact): 지도(위 2/3) + 참가자 리스트/채팅(아래 1/3)
- 태블릿(Expanded): 화면 좌측(또는 우측)에 1/3 크기로 참가자 리스트와 채팅을 고정 패널로 배치하고, 나머지 2/3를 지도로 넓게 채웁니다.
- `ChatInputBar`도 태블릿에서는 하단이 아닌 사이드 패널의 맨 아래에 위치하도록 조정합니다.

## 🔍 검증 방법
- 태블릿 에뮬레이터에서 화면이 좌우로 나뉘어 공간이 효율적으로 사용되는지 확인
- 스마트폰 에뮬레이터에서는 기존처럼 상하 배치로 잘 유지되는지 확인
