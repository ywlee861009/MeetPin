package com.kero.meetpin.app.permission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kero.meetpin.core.location.LocationPermissionHelper

/**
 * 위치·알림 런타임 권한을 최초 진입 시 1회 요청하고, 포그라운드 위치 권한 허용 여부를 반환한다.
 *
 * 권한이 거부되어도 앱은 계속 동작해야 한다 (지도는 기본 위치를 중심으로 표시된다).
 * 따라서 재요청 루프를 만들지 않고, 사용자가 시스템 설정에서 직접 켜고 돌아오는 경로만 반영한다.
 *
 * `ACCESS_BACKGROUND_LOCATION`은 여기서 요청하지 않는다. Android 11+ 정책상 포그라운드 권한
 * 승인 이후 별도 요청이 필요하고, 현재 위치 공유는 화면이 켜진 동안만 필요하다.
 */
@Composable
fun rememberLocationPermissionGranted(): Boolean {
    val context = LocalContext.current

    var isGranted by remember {
        mutableStateOf(LocationPermissionHelper.hasForegroundLocationPermission(context))
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // 결과 맵을 직접 읽지 않고 헬퍼로 재확인한다.
        // 사용자가 '대략적인 위치'만 허용하면 COARSE는 true지만 FINE은 false이므로,
        // 판정 기준(FINE)을 한 곳에서만 관리한다.
        isGranted = LocationPermissionHelper.hasForegroundLocationPermission(context)
    }

    // 프로세스가 살아있는 동안 1회만 요청한다.
    // rememberSaveable로 두어 화면 회전 후 다이얼로그가 다시 뜨지 않게 한다.
    var hasRequested by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!isGranted && !hasRequested) {
            hasRequested = true
            launcher.launch(LocationPermissionHelper.getRequiredPermissions())
        }
    }

    // 설정 앱에서 권한을 바꾸고 복귀하는 경로를 반영한다.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGranted = LocationPermissionHelper.hasForegroundLocationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return isGranted
}
