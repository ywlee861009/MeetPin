package com.meetpin.feature.tracking

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import androidx.compose.ui.platform.LocalContext
import com.meetpin.core.location.LocationTrackingService
import kotlinx.coroutines.flow.collectLatest

/**
 * 실시간 트래킹 지도 화면.
 *
 * - 참가자별 커스텀 아바타 마커
 * - 마커 위치 부드러운 보간 애니메이션 (LatLng Interpolation)
 * - 약속 장소 핀 마커
 */
@SuppressLint("MissingPermission")
@Composable
fun LiveTrackingScreen(
    groupId: String,
    onNavigateToCompletion: (String) -> Unit = {},
    viewModel: LiveTrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            state.pinLocation ?: LatLng(37.5666805, 126.9784147),
            15f
        )
    }

    // 트래킹 시작
    LaunchedEffect(groupId) {
        viewModel.processIntent(LiveTrackingIntent.StartTracking(groupId))
    }

    // Side Effect 수신
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LiveTrackingEffect.AnimateCameraToPosition -> {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(effect.position, 16f),
                        durationMs = 800
                    )
                }
                is LiveTrackingEffect.ShowArrivalCelebration -> {
                    snackbarHostState.showSnackbar("🎉 ${effect.participantName}님이 도착했습니다!")
                }
                is LiveTrackingEffect.NavigateToCompletion -> {
                    onNavigateToCompletion(effect.groupId)
                }
                is LiveTrackingEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is LiveTrackingEffect.StopLocationService -> {
                    LocationTrackingService.stop(context)
                }
            }
        }
    }

    // 핀 위치로 초기 카메라 이동
    LaunchedEffect(state.pinLocation) {
        state.pinLocation?.let { pin ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(pin, 15f)
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 상단 라이브 공유 안내 바
                LiveSharingTopBar(
                    isActive = state.isLocationSharingActive,
                    onStopSharing = {
                        viewModel.processIntent(LiveTrackingIntent.StopLocationSharing)
                    }
                )

                // 지도 영역 (2/3)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = false
                        )
                    ) {
                        // 약속 장소 핀 마커
                        state.pinLocation?.let { pinPos ->
                            Marker(
                                state = MarkerState(position = pinPos),
                                title = state.pinPlaceName,
                                snippet = "약속 장소"
                            )
                        }

                        // 참가자 마커 (부드러운 보간 애니메이션)
                        state.participantMarkers.forEach { marker ->
                            AnimatedParticipantMarker(
                                participantMarker = marker
                            )
                        }
                    }
                }

                // 하단 참가자 상태 시트 (1/3)
                ParticipantStatusSheet(
                    participantMarkers = state.participantMarkers,
                    arrivedCount = state.arrivedCount,
                    totalCount = state.totalCount,
                    onParticipantClick = { userId ->
                        viewModel.processIntent(LiveTrackingIntent.FocusOnParticipant(userId))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 참가자 마커 with 부드러운 위치 보간 애니메이션.
 *
 * targetPosition이 변경될 때 currentPosition에서 targetPosition으로
 * LatLng를 선형 보간(lerp)하여 마커가 부드럽게 이동한다.
 */
@Composable
fun AnimatedParticipantMarker(
    participantMarker: ParticipantMarker
) {
    var animatedLat by remember { mutableStateOf(participantMarker.currentPosition.latitude) }
    var animatedLng by remember { mutableStateOf(participantMarker.currentPosition.longitude) }

    val latAnimatable = remember { Animatable(participantMarker.currentPosition.latitude.toFloat()) }
    val lngAnimatable = remember { Animatable(participantMarker.currentPosition.longitude.toFloat()) }

    // targetPosition 변경 시 애니메이션 실행
    LaunchedEffect(participantMarker.targetPosition) {
        latAnimatable.animateTo(
            targetValue = participantMarker.targetPosition.latitude.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
    }

    LaunchedEffect(participantMarker.targetPosition) {
        lngAnimatable.animateTo(
            targetValue = participantMarker.targetPosition.longitude.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
    }

    animatedLat = latAnimatable.value.toDouble()
    animatedLng = lngAnimatable.value.toDouble()

    val markerState = rememberMarkerState(
        key = participantMarker.participant.userId,
        position = LatLng(animatedLat, animatedLng)
    )

    // 마커 위치 업데이트
    LaunchedEffect(animatedLat, animatedLng) {
        markerState.position = LatLng(animatedLat, animatedLng)
    }

    MarkerComposable(
        state = markerState,
        title = participantMarker.participant.nickname,
        snippet = if (participantMarker.participant.isArrived) {
            "✅ 도착"
        } else {
            "📍 ${formatDistance(participantMarker.distanceToPin)}"
        }
    ) {
        // 커스텀 아바타 마커
        AvatarMarker(
            initial = participantMarker.participant.nickname.take(1),
            isArrived = participantMarker.participant.isArrived
        )
    }
}

/**
 * 참가자 아바타 마커 Composable.
 */
@Composable
private fun AvatarMarker(
    initial: String,
    isArrived: Boolean
) {
    val bgColor = if (isArrived) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = if (isArrived) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * 거리를 사람이 읽기 쉬운 형태로 포맷한다.
 */
fun formatDistance(distanceMeters: Float): String {
    return if (distanceMeters >= 1000) {
        String.format("%.1fkm", distanceMeters / 1000)
    } else {
        String.format("%.0fm", distanceMeters)
    }
}
