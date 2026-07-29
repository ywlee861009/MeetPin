# phase1: Fake 시드를 호스트 + 친구 2명으로 확장

## 문제 / 목표
현재 `FakeMeetPinRepository.createGroup`은 호스트 1 + 친구 1(PENDING)만 만든다.
데모에 필요한 **친구 2명(광화문/강남)** 을 서로 다른 `userId`·닉네임으로 시드한다.

## 수정 대상
- `android/core/data/src/main/java/com/kero/meetpin/core/data/repository/FakeMeetPinRepository.kt`

## 구현 방향
1. `companion object`에 친구 상수 2개 추가:
   ```kotlin
   const val HOST_USER_ID = "user-host"
   const val FRIEND1_USER_ID = "user-friend-1"   // 광화문
   const val FRIEND2_USER_ID = "user-friend-2"   // 강남
   ```
   (기존 `GUEST_USER_ID` 는 friend1로 대체하거나 유지 후 재명명.)
2. `createGroup`의 `participants` 리스트를 3명으로:
   - 호스트: `ACCEPTED`, 닉네임 "나 (호스트)"
   - 친구1: `PENDING`, 닉네임 "광화문 친구"
   - 친구2: `PENDING`, 닉네임 "강남 친구"
   - (선택) `profileImageUrl`에 데모용 이미지 URL/이모지 대체 텍스트를 넣어 아바타 확인.
3. `updateInviteStatus`는 현재 **PENDING 전원을 한 번에** 승낙 처리한다. 데모에서
   두 친구가 동시에 합류해도 무방하면 그대로 둔다. **개별 승낙**을 원하면 시그니처에
   `userId`를 추가하는 것은 phase 범위를 넘으므로 별도 결정(overview의 결정 사항 참고).

## 영향 범위 주의
- 이 Fake는 대기실(`feature/lobby`)·초대 수락 플로우도 함께 쓴다. 친구가 2명이 되면
  대기실 참가자 목록도 2줄로 보인다(정상). 대기실 UI가 인원수를 하드코딩하지 않는지 확인.
- `isAllAccepted`(모두 ACCEPTED)로 그룹이 `ACTIVE` 전이되는 로직은 그대로 유효하다.

## 검증
- [x] `:core:data`, `:app` 컴파일 (EXIT=0)
- [ ] 핀 생성 → 대기실에서 대기 친구 **2명** 표시 확인 — (런타임, phase3 이후 E2E 패스에서 확인)
- [ ] 디버그 `SimulateGuestAccept` 후 트래킹 진입, 참가자 시트에 **3줄**(나+친구2) 표시 — (런타임, phase3 이후 E2E 패스에서 확인)

## 구현 메모
- index 기반 방침 채택: userId는 서로 구분만 되면 되고 좌표는 트래킹 화면이 순번으로 배정.
  → data 모듈 companion 상수를 feature 모듈에 노출할 필요 없음.
- `GUEST_USER_ID` → `FRIEND1_USER_ID`("user-friend-1") / `FRIEND2_USER_ID`("user-friend-2")로 대체.
  소스 참조 없음(빌드 산출물만 stale) 확인.
