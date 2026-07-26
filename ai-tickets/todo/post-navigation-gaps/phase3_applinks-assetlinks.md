# Phase 3: App Links 도메인 검증 (assetlinks.json)

## 🎯 목표

`https://meetpin.app/invite/{code}` 링크가 브라우저가 아니라 MeetPin 앱에서 열리게 한다.

## 📖 문제

Manifest의 intent-filter는 정확하지만 `android:autoVerify="true"` App Link는
**도메인 소유 증명이 없으면 시스템이 앱으로 라우팅하지 않는다.**
`https://meetpin.app/.well-known/assetlinks.json`이 서비스되지 않아 검증이 실패하고,
Android 12+ 에서는 선택 다이얼로그조차 없이 곧바로 브라우저로 넘어간다.

실측 결과:

| 링크 | 결과 |
|---|---|
| `https://meetpin.app/invite/MP0001` (검증 전) | **Chrome이 열림** — 앱 진입 실패 |
| `https://meetpin.app/invite/MP0001` (로컬 승인 후) | 초대 수락 화면 정상 진입 |
| `meetpin://invite?code=MP0001` | **정상 동작** (커스텀 스킴은 검증 불필요) |
| `https://meetpin.app/other` | Chrome이 열림 (pathPrefix가 정확히 좁혀져 있음 — 의도된 동작) |

즉 **코드는 정상이고 도메인 설정만 남았다.** 커스텀 스킴이 있어서 기능 자체가 막히지는 않는다.

## 🛠️ 수정 대상

- `meetpin.app` 웹 서버 (앱 코드 변경 없음)
- 필요 시 `android/README.md`에 개발용 우회 절차 기재

## 📝 구체적인 구현 방향

### 1. assetlinks.json 호스팅

`https://meetpin.app/.well-known/assetlinks.json`에 아래를 서비스한다.

```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.meetpin.app",
    "sha256_cert_fingerprints": ["<release 서명 인증서 SHA-256>"]
  }
}]
```

- `Content-Type: application/json`, HTTP 200, 리다이렉트 없이 응답해야 한다.
- SHA-256은 `./gradlew :app:signingReport`에서 확인한다.
- debug와 release 서명이 다르면 두 지문을 배열에 모두 넣는다.
  현재 debug 서명 SHA-256은 다음과 같다 (에뮬레이터 실측):
  `63:09:8A:DF:62:4D:8E:1C:09:55:75:81:3D:36:C7:4A:2C:2F:01:23:FF:E6:33:74:88:23:A9:E5:37:CB:D1:39`

### 2. 개발 중 우회 (도메인 준비 전)

`README.md`에 아래를 기재해 신규 개발자가 막히지 않게 한다.

```bash
# 로컬에서 도메인을 수동 승인 (검증 없이 앱으로 라우팅)
adb shell pm set-app-links --package com.meetpin.app 2 all
adb shell pm get-app-links com.meetpin.app     # meetpin.app: approved 확인
```

## 🔍 검증 방법

```bash
# 검증 상태 확인 — approved 여야 한다
adb shell pm get-app-links com.meetpin.app

# cold start
adb shell am force-stop com.meetpin.app
adb shell am start -a android.intent.action.VIEW -d "https://meetpin.app/invite/MP0001"
```

- 실기기(에뮬레이터 아님)에서 `pm set-app-links` 없이 링크를 탭했을 때 앱이 열린다.
- Google의 Digital Asset Links 검증 API로 확인한다.
  `https://digitalassetlinks.googleapis.com/v1/statements:list?source.web.site=https://meetpin.app&relation=delegate_permission/common.handle_all_urls`
- 카카오톡·문자로 공유한 링크를 탭해도 앱으로 열린다 (실제 사용 경로).
