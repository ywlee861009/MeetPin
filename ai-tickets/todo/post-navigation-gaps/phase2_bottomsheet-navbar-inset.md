# Phase 2: 핀 생성 BottomSheet의 내비게이션 바 inset 누락

## 🎯 목표

"약속 만들기" 버튼이 시스템 내비게이션 바에 가려지는 문제를 해결한다.

## 📖 문제

`MainActivity`가 `enableEdgeToEdge()`를 호출해 앱이 시스템 바 뒤까지 그려진다.
`CreatePinScreen`은 `BottomSheetScaffold`를 쓰는데, sheet content가 navigation bar inset을
반영하지 않아 **가장 중요한 CTA 버튼이 내비게이션 바에 절반 이상 잘린다.**

에뮬레이터(3버튼 내비게이션, 1080x2400)에서 버튼 상단 약 40px만 노출되어,
탭이 가능하긴 하지만 텍스트가 읽히지 않는다. 제스처 내비게이션에서는 가려지는 양이 줄지만
여전히 핸들 영역과 겹친다.

같은 계열의 확인이 필요한 화면:
- `LiveTrackingScreen` — 참가자 목록 하단이 잘리는지
- `CompletionScreen` — 하단 버튼 위치

## 🛠️ 수정 대상

- `android/feature/map/src/main/java/com/meetpin/feature/map/CreatePinScreen.kt`
  - `PinCreateBottomSheet` 컴포저블 또는 `BottomSheetScaffold` 설정
- `android/feature/tracking/src/main/java/.../LiveTrackingScreen.kt` (확인 후 필요 시)
- `android/feature/tracking/src/main/java/.../CompletionScreen.kt` (확인 후 필요 시)

## 📝 구체적인 구현 방향

sheet content 최하단에 navigation bar inset만큼의 패딩을 넣는다.

```kotlin
Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
```

또는 `Modifier.windowInsetsPadding(WindowInsets.navigationBars)`를 쓴다.

주의점:
- 바깥 `BottomSheetScaffold`가 이미 준 `innerPadding`과 이중 적용되지 않도록 확인한다.
  현재 `CreatePinScreen`은 `innerPadding`을 지도 `Box`에만 적용하고 sheet content에는 쓰지 않는다.
- `sheetPeekHeight`가 `200.dp` 하드코딩이므로, inset을 더하면 peek 상태에서 보이는 영역도 바뀐다.
  peek 높이도 inset을 더한 값으로 계산할지 함께 판단한다.
- 키보드가 올라올 때(약속 이름 입력)의 동작도 함께 본다. `imePadding()`이 필요할 수 있다.
  현재는 플로팅 키보드로 테스트해 이 부분이 검증되지 않았다.

## 🔍 검증 방법

3버튼 내비게이션과 제스처 내비게이션 양쪽에서 확인한다.

```bash
# 3버튼
adb shell cmd overlay enable com.android.internal.systemui.navbar.threebutton
# 제스처
adb shell cmd overlay enable com.android.internal.systemui.navbar.gestural
```

- 핀 생성 화면에서 "약속 만들기" 버튼 전체가 내비게이션 바 위에 온전히 보인다.
- 약속 이름 입력 시 키보드가 버튼을 가리지 않는다(또는 스크롤로 접근 가능하다).
- 실시간 트래킹 화면의 참가자 목록 마지막 항목이 잘리지 않는다.
- 완료 화면의 버튼이 가려지지 않는다.
