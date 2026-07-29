# Phase 1: Google Maps API 키 설정

## 🎯 목표

지도가 회색 화면으로 뜨는 문제를 해결한다.

## 📖 문제

`local.properties`에 `MAPS_API_KEY`가 없어 Maps SDK 인증이 실패한다.

```
E/Google Maps Android API: Error requesting API token. StatusCode=INVALID_ARGUMENT
E/Google Android Maps SDK: Authorization failure. Please see https://developers.google.com/maps/documentation/android-sdk/start
E/Google Android Maps SDK: 	API Key:
```

`app/build.gradle.kts`는 키가 없어도 빌드를 막지 않고 경고만 남기도록 되어 있다(의도된 동작).
결과적으로 지도 타일이 렌더링되지 않고, 핀 생성·실시간 트래킹 화면이 모두 회색으로 보인다.

**지도 탭 이벤트(`onMapClick`)와 줌 컨트롤은 인증 실패 상태에서도 동작한다**는 것은 확인했다.
따라서 이것은 렌더링만의 문제이며, 로직 검증에는 영향이 없다.

## 🛠️ 수정 대상

- `local.properties` (VCS 미추적 — 각 개발자가 로컬에 직접 작성)
- 필요 시 `android/README.md`의 키 발급 절차 보강

## 📝 구체적인 구현 방향

1. Google Cloud Console에서 프로젝트를 만들고 **Maps SDK for Android**를 활성화한다.
2. API 키를 발급하고, 애플리케이션 제한을 Android 앱으로 설정한다.
   - 패키지명: `com.meetpin.app`
   - SHA-1: `./gradlew :app:signingReport`의 debug variant 값
3. `android/local.properties`에 추가한다.
   ```properties
   MAPS_API_KEY=AIza...
   ```
4. `local.properties`가 `.gitignore`에 포함되어 있는지 확인한다.

release 서명 키를 따로 쓸 경우 해당 SHA-1도 키 제한 목록에 추가해야 한다.

## 🔍 검증 방법

```bash
cd android && ./gradlew :app:installDebug
adb logcat -c && adb shell am start -n com.meetpin.app/.MainActivity
adb logcat -d | grep -i "Authorization failure"
```

- `Authorization failure` 로그가 나오지 않는다.
- 핀 생성 화면에 서울시청 주변 지도 타일이 실제로 렌더링된다.
- 실시간 트래킹 화면에 참가자 아바타 마커와 약속 장소 핀이 지도 위에 보인다.
