package com.meetpin.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.meetpin.core.map.LocalMapRenderer
import com.meetpin.core.map.MeetPinMapUiSettings
import com.meetpin.core.model.GeoPoint

/**
 * 메인 지도 화면.
 *
 * 지도 렌더링은 [LocalMapRenderer]로 주입된 벤더 구현에 위임한다.
 * (Google/Kakao/Naver 등 SDK 교체 시 이 화면 코드는 바뀌지 않는다.)
 */
@Composable
fun MapScreen(
    hasLocationPermission: Boolean = false,
    onMapClick: ((GeoPoint) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val mapRenderer = LocalMapRenderer.current

    // 서울 시청 기본 위치 (위치 권한 없을 때)
    val defaultPosition = remember { GeoPoint(37.5666805, 126.9784147) }
    val cameraState = mapRenderer.rememberCameraState(defaultPosition, 15f)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            mapRenderer.Map(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                myLocationEnabled = hasLocationPermission,
                uiSettings = MeetPinMapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true,
                    compassEnabled = true,
                ),
                onMapClick = onMapClick,
            )
        }
    }

    // 위치 권한 없을 때 안내 스낵바
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            snackbarHostState.showSnackbar("위치 권한을 허용하면 내 위치를 표시할 수 있습니다.")
        }
    }
}
