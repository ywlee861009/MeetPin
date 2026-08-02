package com.kero.meetpin.core.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * 출발 알림(로컬 Notification) 발송기.
 *
 * "지금 출발해야 늦지 않아요!" 알림을 띄우고, 탭하면 해당 약속의 실시간 지도로 진입하는
 * 딥링크([TRACK_DEEP_LINK_PREFIX] + groupId)를 건다. 딥링크 라우팅은 앱의 네비게이션
 * 그래프가 담당한다.
 *
 * [DepartureCheckWorker]가 백그라운드에서 직접 생성해 호출하므로 Hilt에 의존하지 않는다.
 */
class DepartureNotifier(private val context: Context) {

    /**
     * 출발 알림을 띄운다. Android 13+ 에서 알림 권한이 없으면 조용히 무시한다.
     *
     * @param groupId 대상 약속 id (딥링크·알림 id로 사용)
     * @param groupTitle 약속 제목
     * @param placeName 목적지 이름
     * @param etaMinutes 예상 소요 시간(분)
     */
    fun notifyDeparture(
        groupId: String,
        groupTitle: String,
        placeName: String,
        etaMinutes: Int
    ) {
        if (!hasNotificationPermission()) return

        createChannel()

        val notificationId = groupId.hashCode()
        val contentTitle = groupTitle.ifBlank { DEFAULT_TITLE }
        val contentText = buildString {
            if (placeName.isNotBlank()) append(placeName).append("까지 ")
            append("약 ").append(etaMinutes).append("분 · 지금 출발하세요")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(buildTrackingIntent(groupId, notificationId))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * 알림 탭 시 실시간 지도로 진입하는 PendingIntent.
     * `meetpin://track/{groupId}` 딥링크를 자기 앱으로만 라우팅되도록 패키지를 고정한다.
     */
    private fun buildTrackingIntent(groupId: String, requestCode: Int): PendingIntent {
        val uri = Uri.parse("$TRACK_DEEP_LINK_PREFIX$groupId")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "약속 시간에 맞춰 출발을 안내하는 알림"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    /**
     * Android 13(TIRAMISU)+ 에서만 런타임 알림 권한이 필요하다. 그 이전은 항상 허용으로 본다.
     */
    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        /** 알림 탭 → 실시간 지도 딥링크 접두. groupId를 뒤에 붙인다. (manifest·NavHost와 일치) */
        const val TRACK_DEEP_LINK_PREFIX = "meetpin://track/"

        private const val CHANNEL_ID = "meetpin_departure_channel"
        private const val CHANNEL_NAME = "출발 알림"
        private const val DEFAULT_TITLE = "지금 출발해야 늦지 않아요!"
    }
}
