# Phase 2: MAPS_API_KEY placeholder 배선

> **상태: 완료 (2026-07-26)**
>
> - `app/build.gradle.kts`: `local.properties`에서 `MAPS_API_KEY`를 읽어 `manifestPlaceholders`에 주입.
>   키 부재 시 `logger.warn`만 남기고 빌드는 통과시킴
> - `android/README.md`: "최초 설정: Google Maps API 키" 절과 "빌드 및 검증 명령" 절 추가
> - 검증: `./gradlew :app:processDebugMainManifest` → `BUILD SUCCESSFUL`,
>   병합 Manifest 54행에서 placeholder가 치환됨 확인
> - **미완 사항**: 실제 API 키는 주입하지 못했습니다(발급 권한이 없음). 현재 병합 결과는
>   `android:value=""`이므로 앱 실행 시 지도가 회색으로 표시됩니다.
>   사용자가 직접 `android/local.properties`에 `MAPS_API_KEY=...` 한 줄을 추가해야 합니다.

## 🎯 목표
- `AndroidManifest.xml`이 참조하는 `${MAPS_API_KEY}`를 빌드 스크립트에서 실제로 치환한다.
- API 키를 VCS에 커밋하지 않고 `local.properties`에서 주입한다.
- 키가 없는 환경에서도 빌드가 실패하지 않고 명확한 안내와 함께 진행되도록 한다.

## 🛠️ 수정 대상
- `android/app/build.gradle.kts`
- `android/local.properties` (커밋 대상 아님, 로컬에만 키 추가)
- `android/README.md` (키 설정 절차 문서화)
- `.gitignore` (`local.properties` 제외 여부 확인)

## 📝 구체적인 구현 방향
- 현재 오류
  ```
  AndroidManifest.xml:30:13-44 Error: Attribute meta-data#com.google.android.geo.API_KEY@value
  requires a placeholder substitution but no value for <MAPS_API_KEY> is provided.
  ```
  `manifestPlaceholders` 설정이 어디에도 없어 어느 머신에서도 debug 빌드가 불가능한 상태다.
- `app/build.gradle.kts`의 `defaultConfig`에서 `local.properties`를 읽어 주입한다.
  ```kotlin
  val mapsApiKey: String = Properties().apply {
      rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
  }.getProperty("MAPS_API_KEY") ?: ""
  manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
  ```
- 키가 비어 있으면 빌드는 통과시키되 `logger.warn`으로 "지도가 회색으로 표시된다"는 경고를 남긴다
  (빌드 자체를 막으면 CI/신규 개발자 온보딩이 막힌다).
- `local.properties`에 `MAPS_API_KEY=...` 한 줄을 추가한다. 이 파일은 커밋하지 않는다.
- `android/README.md`에 Google Cloud Console에서 Maps SDK for Android 키를 발급받아
  `local.properties`에 넣는 절차를 명시한다.

## 🔍 검증 방법
```bash
./gradlew :app:processDebugMainManifest
```
- `BUILD SUCCESSFUL`이어야 한다.
- 병합 결과 Manifest(`app/build/intermediates/merged_manifest*/AndroidManifest.xml`)에서
  `com.google.android.geo.API_KEY`의 `android:value`가 placeholder 문자열이 아닌 실제 값으로
  치환되었는지 확인한다.
- `git status`에 `local.properties`가 커밋 대상으로 올라오지 않아야 한다.
