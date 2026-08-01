package com.kero.meetpin.app.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 위치 권한이 없는 동안 화면 상단에 안내 배너를 덧붙이는 게이트.
 *
 * 권한을 강제하지 않는 기존 설계([rememberLocationPermissionGranted])와 일관되게,
 * 모달 다이얼로그로 흐름을 막는 대신 **비침습적 배너**로 상황을 알리고 시스템 설정으로
 * 이동하는 경로만 제공한다. 사용자가 설정에서 권한을 켜고 복귀하면(ON_RESUME 재확인)
 * [hasPermission]이 true가 되어 배너가 사라진다.
 */
@Composable
fun LocationPermissionScaffold(
    hasPermission: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (!hasPermission) {
            LocationPermissionBanner()
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
private fun LocationPermissionBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "📍", style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "위치 권한이 꺼져 있어요",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "내 위치가 지도에 표시되지 않습니다. 설정에서 위치 권한을 허용해 주세요.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(
                onClick = { context.openAppSettings() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(text = "설정 열기", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** 이 앱의 시스템 설정 상세 화면을 연다(권한 토글 경로). */
private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
    startActivity(intent)
}
