package com.kero.meetpin.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * 위치 권한 상태를 확인하는 유틸리티 헬퍼.
 */
object LocationPermissionHelper {

    /**
     * 포그라운드 위치 권한(ACCESS_FINE_LOCATION)이 허용되었는지 확인.
     */
    fun hasForegroundLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 백그라운드 위치 권한(ACCESS_BACKGROUND_LOCATION)이 허용되었는지 확인.
     * Android 10(Q) 이상에서만 필요.
     */
    fun hasBackgroundLocationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Q 미만에서는 포그라운드 권한만 있으면 됨
        }
    }

    /**
     * 알림 권한(POST_NOTIFICATIONS)이 허용되었는지 확인.
     * Android 13(TIRAMISU) 이상에서만 필요.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * 실시간 위치 공유에 필요한 모든 필수 권한 목록을 반환.
     */
    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions.toTypedArray()
    }

    /**
     * 백그라운드 위치 권한을 별도로 요청해야 하는 권한 (Android 10+).
     */
    fun getBackgroundLocationPermission(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        } else {
            null
        }
    }
}
