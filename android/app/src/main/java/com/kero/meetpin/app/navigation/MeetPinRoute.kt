package com.kero.meetpin.app.navigation

import android.net.Uri

/**
 * MeetPin 앱의 네비게이션 목적지 정의.
 *
 * 각 목적지는 `NavHost` 등록용 **패턴 상수**와, 인자를 채워 실제 경로를 만드는 **팩토리 함수**를
 * 짝으로 갖는다. 인자 키는 상수로 노출해 `NavBackStackEntry`에서 값을 꺼낼 때
 * 문자열을 중복 작성하지 않도록 한다.
 */
object MeetPinRoute {

    const val ARG_GROUP_ID = "groupId"
    const val ARG_GROUP_TITLE = "groupTitle"
    const val ARG_INVITE_CODE = "inviteCode"

    /** 시작 목적지 — 지도에서 약속 핀을 찍고 약속 정보를 입력하는 화면. */
    const val CREATE_PIN = "createPin"

    /**
     * 초대 공유 화면.
     *
     * 약속 제목은 사용자가 직접 입력한 자유 문자열(한글·공백·`/` 포함 가능)이므로
     * path argument가 아닌 **query argument**로 둔다.
     * path에 인코딩된 `%2F`가 들어가면 Navigation의 경로 매칭이 깨진다.
     */
    const val INVITE_SHARE =
        "inviteShare/{$ARG_GROUP_ID}/{$ARG_INVITE_CODE}?$ARG_GROUP_TITLE={$ARG_GROUP_TITLE}"

    fun inviteShare(groupId: String, groupTitle: String, inviteCode: String): String =
        "inviteShare/$groupId/$inviteCode?$ARG_GROUP_TITLE=${Uri.encode(groupTitle)}"

    /** 참가자 전원 승낙을 기다리는 대기실. */
    const val LOBBY = "lobby/{$ARG_GROUP_ID}"

    fun lobby(groupId: String): String = "lobby/$groupId"

    /** 실시간 위치 공유 화면. */
    const val LIVE_TRACKING = "liveTracking/{$ARG_GROUP_ID}"

    fun liveTracking(groupId: String): String = "liveTracking/$groupId"

    /** 전원 도착 후 약속 종료 화면. */
    const val COMPLETION = "completion/{$ARG_GROUP_ID}"

    fun completion(groupId: String): String = "completion/$groupId"

    /** 딥링크로 초대를 받은 사용자의 수락/거절 화면. */
    const val INVITE_ACCEPT = "inviteAccept/{$ARG_INVITE_CODE}"

    fun inviteAccept(inviteCode: String): String = "inviteAccept/$inviteCode"
}
