package com.kero.meetpin.feature.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kero.meetpin.core.designsystem.component.PinMarker
import com.kero.meetpin.core.map.LocalMapRenderer
import com.kero.meetpin.core.map.MeetPinMapUiSettings
import com.kero.meetpin.core.model.GeoPoint
import kotlinx.coroutines.flow.collectLatest

/**
 * 핀 생성 화면 (지도 only).
 *
 * 화면 중앙에 고정 핀이 있고, 그 위 "약속 만들기" 말풍선을 누르면
 * **지도 중앙 좌표**로 약속을 만들고 바로 실시간 지도로 넘어간다.
 * 제목·일시 입력, 대기실은 없다 (즉시 공유 플로우).
 */
@Composable
fun CreatePinScreen(
    hasLocationPermission: Boolean = false,
    onNavigateToLiveTracking: (groupId: String) -> Unit = {},
    viewModel: CreatePinViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val mapRenderer = LocalMapRenderer.current

    // 서울 시청 기본 위치
    val defaultPosition = remember { GeoPoint(37.5666805, 126.9784147) }
    val cameraState = mapRenderer.rememberCameraState(defaultPosition, 15f)

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PinCreateEffect.NavigateToLiveTracking -> onNavigateToLiveTracking(effect.groupId)
                is PinCreateEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            mapRenderer.Map(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                myLocationEnabled = hasLocationPermission,
                uiSettings = MeetPinMapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = hasLocationPermission
                ),
                contentPadding = WindowInsets.systemBars.asPaddingValues(),
                onMapClick = null,
                content = {}
            )

            // 화면 중앙 고정 핀 + "약속 만들기" 말풍선
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    // 핀 끝이 대략 화면 중앙에 오도록 살짝 위로 올린다.
                    .offset(y = (-24).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    onClick = {
                        // 지도 중앙 좌표로 바로 생성한다.
                        viewModel.processIntent(PinCreateIntent.SelectLocation(cameraState.position))
                        viewModel.processIntent(PinCreateIntent.SubmitPin)
                    },
                    enabled = !state.isSubmitting,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "약속 만들기",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "여기서 만나요",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 고정 핀 (디자인 시스템 마커)
                PinMarker(size = 40.dp)
            }
        }
    }
}
