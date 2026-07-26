# Phase 3: 딥링크 진입 처리

## 🎯 목표

- 초대 딥링크를 탭했을 때 `InviteAcceptScreen` 으로 진입시킨다.
- 앱이 꺼진 상태(cold start)와 실행 중인 상태(warm start) 양쪽을 처리한다.
- `DeepLinkHandler` 가 지원하는 두 형식(`https://meetpin.app/invite/{code}`, `meetpin://invite?code={code}`)을
  Manifest intent-filter와 일치시킨다.

## 📖 문제

`AndroidManifest.xml` 에 `https://meetpin.app/invite` 에 대한 `autoVerify` intent-filter가 선언되어 있고
`DeepLinkHandler.extractInviteCode(intent)` 도 구현되어 있으나, **호출부가 0건**이다.
따라서 링크를 탭하면 앱이 실행되기만 하고 초대 수락 화면이 나오지 않는다.

또한 `DeepLinkHandler` 는 `meetpin://invite?code=` 커스텀 스킴도 파싱하지만
Manifest에는 해당 intent-filter가 없어 실제로는 도달할 수 없다.

## 🛠️ 수정 대상

- `android/app/src/main/AndroidManifest.xml` — `launchMode`, 커스텀 스킴 intent-filter
- `android/app/src/main/java/com/meetpin/app/MainActivity.kt` — 딥링크 파싱 및 전달
- `android/app/src/main/java/com/meetpin/app/navigation/MeetPinNavHost.kt` — 시작 목적지 분기

## 📝 구체적인 구현 방향

### 1. Manifest

`MainActivity` 에 `android:launchMode="singleTop"` 을 추가한다.
현재는 기본값(`standard`)이라 앱이 실행 중일 때 링크를 탭하면 `MainActivity` 인스턴스가
하나 더 생성되어 백스택에 쌓인다.

커스텀 스킴 intent-filter를 추가한다.

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="meetpin" android:host="invite" />
</intent-filter>
```

`autoVerify="true"` 는 커스텀 스킴에 적용되지 않으므로 https 필터와 **분리된 별도 filter**로 둔다.
하나의 filter에 두 스킴을 합치면 `meetpin://` 조합까지 교차 매칭되어 의도치 않은 URL을 받게 된다.

### 2. Cold start

`MainActivity.onCreate` 에서 `DeepLinkHandler.extractInviteCode(intent)` 를 호출하고,
결과가 있으면 `MeetPinNavHost(startInviteCode = code)` 로 넘겨 시작 목적지를
`inviteAccept/{code}` 로 바꾼다. `null` 이면 기존 `createPin` 을 유지한다.

`navController.navigate()` 로 처리하지 않고 시작 목적지 자체를 바꾸는 이유:
`createPin` 을 먼저 띄운 뒤 이동하면 한 프레임 동안 핀 생성 화면이 깜빡이고,
뒤로가기 시 초대받은 사용자가 핀 생성 화면으로 떨어진다.

### 3. Warm start

`launchMode="singleTop"` 이므로 실행 중 재진입은 `onNewIntent` 로 들어온다.
Activity에 `MutableStateFlow<String?>` 을 두고 `onNewIntent` 에서 초대 코드를 emit,
Compose 쪽에서 `collectAsStateWithLifecycle` 로 받아 `LaunchedEffect` 에서 navigate한다.

`onNewIntent` 에서 `setIntent(intent)` 를 반드시 호출한다 —
호출하지 않으면 이후 `getIntent()` 가 최초 intent를 계속 반환한다.

같은 초대 코드로 재진입했을 때 목적지가 중복 push되지 않도록
`launchSingleTop = true` 를 지정하고, 처리한 코드는 emit 후 `null` 로 되돌려
구성 변경(회전) 시 재이동이 일어나지 않게 한다.

## 🔍 검증 방법

```bash
cd android && ./gradlew :app:assembleDebug
```

ADB로 두 형식 모두 확인한다.

```bash
# cold start (앱 강제 종료 후)
adb shell am force-stop com.meetpin.app
adb shell am start -a android.intent.action.VIEW -d "https://meetpin.app/invite/TESTCODE"
adb shell am start -a android.intent.action.VIEW -d "meetpin://invite?code=TESTCODE"

# warm start (앱 실행 중)
adb shell am start -a android.intent.action.VIEW -d "https://meetpin.app/invite/TESTCODE"
```

- cold start: 앱이 실행되며 곧바로 초대 수락 화면이 나온다. 핀 생성 화면이 깜빡이지 않는다.
- cold start 후 뒤로가기: 앱이 종료된다 (핀 생성 화면으로 떨어지지 않는다).
- warm start: `MainActivity` 가 새로 생성되지 않고 현재 화면 위에 초대 수락 화면이 올라온다.
- 같은 링크를 연속 2회 탭해도 초대 수락 화면이 2개 쌓이지 않는다.
- 초대 수락 화면에서 화면 회전 시 같은 화면이 다시 push되지 않는다.
- 수락 → 대기실 진입, 뒤로가기 시 초대 수락 화면으로 돌아가지 않는다.
- 잘못된 링크(`https://meetpin.app/other`)로는 앱이 열리지 않는다.
