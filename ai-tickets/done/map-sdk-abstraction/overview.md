# map-sdk-abstraction — 지도 SDK 교체 가능 추상화

## 🎯 목표

지도 SDK(현재 Google Maps)를 언제든 카카오맵/네이버맵 등으로 교체할 수 있도록
벤더 중립 인터페이스 뒤로 격리한다. 화면·ViewModel·상태(state) 계층에서
`com.google.*` 타입을 전부 제거한다.

## 🏗️ 구조

```
:core:model      → GeoPoint (벤더 중립 좌표)
:core:map        → MapRenderer 인터페이스, 중립 타입, LocalMapRenderer (벤더 의존성 0)
:core:map-google → GoogleMapRenderer : MapRenderer (구글 SDK 격리)
:app             → 루트에서 CompositionLocalProvider(LocalMapRenderer provides GoogleMapRenderer())
feature:*        → :core:map(인터페이스)만 의존
```

교체 절차: `:core:map-<vendor>` 모듈에 Renderer 구현 추가 → 앱 루트 provider 한 줄 교체 → Gradle 의존성 교체.

## 📝 세부 작업

- [x] `:core:model`에 `GeoPoint` 추가
- [x] `:core:map` 모듈: `MapRenderer`, `MapCameraState`, `MapMarkerScope`, `MeetPinMapUiSettings`, `LocalMapRenderer`
- [x] `:core:map-google` 모듈: `GoogleMapRenderer` 구현 (projection 오프스크린 변환 포함)
- [x] settings.gradle 모듈 등록, app 배선(CompositionLocal 주입)
- [x] `feature:map` 이관 (MapScreen, CreatePinScreen, PinCreateContract/ViewModel), 구글 의존성 제거
- [x] `feature:tracking` 이관 (LiveTrackingScreen, Contract/ViewModel), 구글 의존성 제거

## 🔍 검증 기준

- `feature:*` 모듈에서 `com.google.*` import 0건
- 전체 빌드 성공 및 도메인 테스트 통과
- 회색 지도 이슈와 독립 (MAPS_API_KEY는 별도 티켓)

## ⚠️ 알려진 제약

- 오프스크린 말풍선의 `projection.toScreenLocation()`은 SDK별 차이가 커
  `MapCameraState.toScreenOffset(): Offset?` escape hatch로 노출한다.
- Compose 지도 렌더링은 런타임 DI가 아니라 `@Composable` 인터페이스 + CompositionLocal로 주입한다.
