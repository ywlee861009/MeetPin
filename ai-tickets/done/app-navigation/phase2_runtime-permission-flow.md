# Phase 2: 런타임 권한 요청 흐름 및 위치 서비스 시작 연결

## 🎯 목표

- 위치(`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`)와 알림(`POST_NOTIFICATIONS`) 런타임 권한을 실제로 요청한다.
- 권한 결과를 `MapScreen` / `CreatePinScreen` / `LiveTrackingScreen` 에 주입해 내 위치 표시를 활성화한다.
- `LocationTrackingService.start()` 를 실시간 추적 진입 시 호출한다.

## 📖 문제

`LocationPermissionHelper` 는 구현되어 있으나 호출부가 0건이다.
`MapScreen(hasLocationPermission = false)` / `CreatePinScreen(hasLocationPermission = false)` 의
기본값이 그대로 쓰이고 있어, Manifest에 권한이 선언되어 있어도 런타임 요청이 일어나지 않는다.
결과적으로 `isMyLocationEnabled = false`, `myLocationButtonEnabled = false` 로 고정된다.

또한 `LocationTrackingService.stop()` 은 `LiveTrackingScreen` 에서 호출되지만
`start()` 호출부가 없어 포그라운드 위치 공유가 시작되지 않는다.

## 🛠️ 수정 대상

- `android/app/src/main/java/com/meetpin/app/permission/LocationPermissionState.kt` (신규)
- `android/app/src/main/java/com/meetpin/app/navigation/MeetPinNavHost.kt` — 권한 상태 주입
- `android/feature/tracking/src/main/java/com/meetpin/feature/tracking/LiveTrackingScreen.kt` — 서비스 시작

## 📝 구체적인 구현 방향

### 1. 권한 상태 Composable

```kotlin
@Composable
fun rememberLocationPermissionState(): LocationPermissionState
```

- `rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions())` 사용.
- 요청할 권한 목록은 `LocationPermissionHelper.getRequiredPermissions()` 에서 가져온다
  (SDK 33+ 에서만 `POST_NOTIFICATIONS` 를 포함하도록 이미 분기되어 있음).
- 초기값은 `LocationPermissionHelper.hasForegroundLocationPermission(context)` 로 채운다.
- 최초 진입 시 `LaunchedEffect` 로 1회만 요청한다. 거부되어도 앱은 계속 동작해야 하며
  (기본 위치 = 서울시청), 재요청 루프에 빠지지 않도록 한다.
- `ON_RESUME` 에서 권한 상태를 재확인한다 — 사용자가 설정에서 권한을 켜고 돌아오는 경로를 반영.

`ACCESS_BACKGROUND_LOCATION` 은 **여기서 요청하지 않는다.** Android 11+ 정책상 포그라운드 권한
승인 이후에 별도 요청해야 하며, 요청 시점은 실시간 추적 진입 시점이 적절하다.
현재 `LocationTrackingService` 는 `foregroundServiceType="location"` 으로 동작하므로
포그라운드 권한만으로 화면이 켜진 동안의 공유는 성립한다. 백그라운드 권한 요청은 별도 티켓으로 분리한다.

### 2. 서비스 시작

`LiveTrackingScreen` 에서 `DisposableEffect(groupId)` 로 진입 시 `LocationTrackingService.start(context)`,
이탈 시 `LocationTrackingService.stop(context)` 를 호출한다.
`LiveTrackingEffect.StopLocationService` 를 통한 기존 종료 경로와 이중 호출이 발생할 수 있으나
`stop()` 은 멱등해야 하므로 `LocationTrackingService.stopTracking()` 이 이미 중지된 상태에서도
안전한지 확인한다.

포그라운드 위치 권한이 없으면 서비스를 시작하지 않고 스낵바로 안내한다
(권한 없이 `foregroundServiceType=location` 서비스를 시작하면 `SecurityException` 이 발생).

## 🔍 검증 방법

```bash
cd android && ./gradlew :app:assembleDebug
```

- 앱 최초 실행 시 위치 권한 다이얼로그가 표시된다.
- 허용 시: 지도에 내 위치 파란 점과 우측 상단 내 위치 버튼이 나타난다.
- 거부 시: 앱이 크래시하지 않고 서울시청 기준 지도가 표시된다.
- 거부 후 시스템 설정에서 권한을 허용하고 앱으로 복귀하면 내 위치가 활성화된다.
- 실시간 추적 화면 진입 시 상단 알림 영역에 위치 공유 포그라운드 알림이 표시된다.
- 실시간 추적 화면을 벗어나면 해당 알림이 사라진다.
- `grep -rn "hasLocationPermission = false" android/` 결과가 기본값 선언부 외에 없다.
