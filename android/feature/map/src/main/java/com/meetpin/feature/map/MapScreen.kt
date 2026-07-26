package com.meetpin.feature.map

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Google Maps 기반 메인 지도 화면.
 *
 * - GoogleMap Compose 컴포넌트 렌더링
 * - 내 위치 표시 (isMyLocationEnabled)
 * - 내 위치 바로가기 버튼
 * - 지도 클릭 시 콜백 전달 (핀 생성 등)
 */
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    hasLocationPermission: Boolean = false,
    onMapClick: ((LatLng) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // 서울 시청 기본 위치 (위치 권한 없을 때)
    val defaultPosition = LatLng(37.5666805, 126.9784147)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 15f)
    }

    val mapProperties = remember(hasLocationPermission) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission,
            mapType = MapType.NORMAL,
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
            compassEnabled = true,
            mapToolbarEnabled = false,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
                onMapClick = { latLng ->
                    onMapClick?.invoke(latLng)
                }
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
