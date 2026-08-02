package com.kero.meetpin.feature.lobby

import android.content.Intent
import android.net.Uri

/**
 * MeetPin 딥링크 파서.
 *
 * 딥링크 URL에서 초대 코드(inviteCode) 또는 출발 알림의 약속 id(groupId)를 추출한다.
 * 지원 형식:
 * - https://meetpin.app/invite/{inviteCode}
 * - meetpin://invite?code={inviteCode}
 * - meetpin://track/{groupId}   (출발 알림 → 실시간 지도)
 */
object DeepLinkHandler {

    private const val HOST_WEB = "meetpin.app"
    private const val HOST_APP = "invite"
    private const val HOST_TRACK = "track"
    private const val PATH_PREFIX = "/invite/"
    private const val QUERY_CODE = "code"

    /**
     * Intent에서 초대 코드를 추출한다.
     *
     * @param intent Activity로 전달된 Intent
     * @return 초대 코드 또는 null (딥링크가 아닌 경우)
     */
    fun extractInviteCode(intent: Intent?): String? {
        val data = intent?.data ?: return null
        return extractInviteCode(data)
    }

    /**
     * Uri에서 초대 코드를 추출한다.
     */
    fun extractInviteCode(uri: Uri): String? {
        return when {
            // https://meetpin.app/invite/{inviteCode}
            uri.host == HOST_WEB && uri.path?.startsWith(PATH_PREFIX) == true -> {
                uri.path?.removePrefix(PATH_PREFIX)?.takeIf { it.isNotBlank() }
            }
            // meetpin://invite?code={inviteCode}
            uri.scheme == "meetpin" -> {
                uri.getQueryParameter(QUERY_CODE)?.takeIf { it.isNotBlank() }
            }
            else -> null
        }
    }

    /**
     * Intent에서 출발 알림 딥링크(meetpin://track/{groupId})의 약속 id를 추출한다.
     *
     * @return 약속 id 또는 null (출발 알림 딥링크가 아닌 경우)
     */
    fun extractTrackingGroupId(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        if (uri.scheme != "meetpin" || uri.host != HOST_TRACK) return null
        return uri.lastPathSegment?.takeIf { it.isNotBlank() }
    }
}
