package com.kero.meetpin.app.navigation

/**
 * MeetPin 앱의 네비게이션 목적지 정의.
 *
 * 각 목적지는 `NavHost` 등록용 **패턴 상수**와, 인자를 채워 실제 경로를 만드는 **팩토리 함수**를
 * 짝으로 갖는다. 인자 키는 상수로 노출해 `NavBackStackEntry`에서 값을 꺼낼 때
 * 문자열을 중복 작성하지 않도록 한다.
 *
 * 즉시-공유 플로우: 핀 생성 → 실시간 지도, 초대 수락 → 실시간 지도.
 * 대기실·초대공유·완료 목적지는 없다.
 */
object MeetPinRoute {

    const val ARG_GROUP_ID = "groupId"
    const val ARG_INVITE_CODE = "inviteCode"

    /** 시작 목적지 — 지도 중앙 핀으로 약속을 만드는 화면. */
    const val CREATE_PIN = "createPin"

    /** 실시간 위치 공유 화면. */
    const val LIVE_TRACKING = "liveTracking/{$ARG_GROUP_ID}"

    fun liveTracking(groupId: String): String = "liveTracking/$groupId"

    /** 딥링크로 초대를 받은 사용자의 수락/거절 화면. */
    const val INVITE_ACCEPT = "inviteAccept/{$ARG_INVITE_CODE}"

    fun inviteAccept(inviteCode: String): String = "inviteAccept/$inviteCode"
}
