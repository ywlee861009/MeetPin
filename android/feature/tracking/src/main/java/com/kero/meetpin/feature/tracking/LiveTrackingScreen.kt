package com.kero.meetpin.feature.tracking

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kero.meetpin.core.designsystem.component.MeetPinAvatar
import com.kero.meetpin.core.designsystem.component.MeetPinButton
import com.kero.meetpin.core.designsystem.component.MeetPinChatBubble
import com.kero.meetpin.core.designsystem.component.MeetPinSecondaryButton
import com.kero.meetpin.core.designsystem.component.MeetPinTextField
import com.kero.meetpin.core.location.LocationTrackingService
import com.kero.meetpin.core.map.LocalMapRenderer
import com.kero.meetpin.core.map.MapMarkerScope
import com.kero.meetpin.core.map.MeetPinMapUiSettings
import com.kero.meetpin.core.model.GeoPoint
import kotlinx.coroutines.flow.collectLatest

/**
 * 실시간 트래킹 지도 화면.
 *
 * - 참가자별 커스텀 아바타 마커
 * - 마커 위치 부드러운 보간 애니메이션 (좌표 Interpolation)
 * - 약속 장소 핀 마커
 *
 * 지도 렌더링은 [LocalMapRenderer]로 주입된 벤더 구현에 위임한다.
 * UI 원자는 모두 `:core:designsystem` 컴포넌트를 쓴다(M3 원자 직접 사용 금지).
 */
@Composable
fun LiveTrackingScreen(
    groupId: String,
    hasLocationPermission: Boolean = false,
    showDebugTools: Boolean = false,
    viewModel: LiveTrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val mapRenderer = LocalMapRenderer.current
    var mapSize by remember { mutableStateOf(IntSize.Zero) }

    val defaultPosition = remember { GeoPoint(37.5666805, 126.9784147) }
    val cameraState = mapRenderer.rememberCameraState(
        initialPosition = state.pinLocation ?: defaultPosition,
        initialZoom = 15f,
    )

    // 트래킹 시작
    LaunchedEffect(groupId) {
        viewModel.processIntent(LiveTrackingIntent.StartTracking(groupId))
    }

    // 위치 공유 포그라운드 서비스의 수명을 이 화면의 수명에 맞춘다.
    // 권한이 없으면 시작하지 않는다 — foregroundServiceType="location" 서비스를 권한 없이
    // 시작하면 SecurityException으로 앱이 죽는다.
    DisposableEffect(groupId, hasLocationPermission) {
        if (hasLocationPermission) {
            LocationTrackingService.start(context)
        }
        onDispose {
            LocationTrackingService.stop(context)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            snackbarHostState.showSnackbar("위치 권한이 없어 내 위치를 공유할 수 없습니다.")
        }
    }

    // Side Effect 수신
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LiveTrackingEffect.AnimateCameraToPosition -> {
                    cameraState.animate(effect.position, zoom = 16f, durationMs = 800)
                }
                is LiveTrackingEffect.ShowParticipantJoined -> {
                    snackbarHostState.showSnackbar("🎉 ${effect.participantName}님이 참여했습니다!")
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
            cameraState.animate(pin, zoom = 15f)
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

                // 초대 공유
                MeetPinButton(
                    text = "🔗 초대 링크 공유",
                    onClick = { shareInviteLink(context, state.inviteCode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // (디버그) 상대 수락 / 친구별 채팅 시뮬
                if (showDebugTools) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MeetPinSecondaryButton(
                            text = "🧪 상대 수락",
                            onClick = {
                                viewModel.processIntent(LiveTrackingIntent.SimulateGuestAccept)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MeetPinSecondaryButton(
                                text = "🧪 광화문 친구",
                                onClick = {
                                    viewModel.processIntent(
                                        LiveTrackingIntent.SimulateGuestChat(friendIndex = 0)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            MeetPinSecondaryButton(
                                text = "🧪 강남 친구",
                                onClick = {
                                    viewModel.processIntent(
                                        LiveTrackingIntent.SimulateGuestChat(friendIndex = 1)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 지도 영역 (2/3)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                        .onGloballyPositioned { coordinates ->
                            mapSize = coordinates.size
                        }
                ) {
                    mapRenderer.Map(
                        modifier = Modifier.fillMaxSize(),
                        cameraState = cameraState,
                        myLocationEnabled = true,
                        uiSettings = MeetPinMapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = false,
                        ),
                        // 지도 영역은 Scaffold innerPadding으로 이미 시스템 바 밖에 있으므로
                        // 컨트롤에 추가 패딩이 필요 없다 (중복 인셋 방지).
                        contentPadding = PaddingValues(0.dp),
                        onMapClick = null,
                    ) {
                        // 약속 장소 핀 마커
                        state.pinLocation?.let { pinPos ->
                            Marker(
                                position = pinPos,
                                title = state.pinPlaceName,
                                snippet = "약속 장소",
                            )
                        }

                        // 참가자 마커 (부드러운 보간 애니메이션)
                        state.participantMarkers.forEach { marker ->
                            AnimatedParticipantMarker(participantMarker = marker)
                        }
                    }

                    // 오프스크린 말풍선 오버레이
                    if (mapSize.width > 0 && mapSize.height > 0) {
                        state.participantMarkers
                            .filter { !it.chatMessage.isNullOrEmpty() }
                            .forEach { marker ->
                                val screen = cameraState.toScreenOffset(marker.targetPosition)
                                    ?: return@forEach
                                val w = mapSize.width
                                val h = mapSize.height

                                // 화면 밖인지 체크 (패딩 여유 40px)
                                if (screen.x < -40 || screen.y < -40 ||
                                    screen.x > w + 40 || screen.y > h + 40
                                ) {
                                    val cx = w / 2f
                                    val cy = h / 2f
                                    val dx = screen.x - cx
                                    val dy = screen.y - cy

                                    val slope = if (dx != 0f) dy / dx else 1000000f

                                    val margin = 100f // 모서리 여백
                                    val xEdge = if (dx > 0) w.toFloat() - margin else margin
                                    val yIntersection = cy + slope * (xEdge - cx)

                                    var intersectX = xEdge
                                    var intersectY = yIntersection

                                    if (yIntersection < margin || yIntersection > h.toFloat() - margin) {
                                        val yEdge = if (dy > 0) h.toFloat() - margin else margin
                                        val xIntersection = cx + (yEdge - cy) / slope
                                        intersectX = xIntersection
                                        intersectY = yEdge
                                    }

                                    MeetPinChatBubble(
                                        text = marker.chatMessage ?: "",
                                        isMine = false,
                                        modifier = Modifier.absoluteOffset(
                                            x = with(LocalDensity.current) { intersectX.toDp() } - 30.dp,
                                            y = with(LocalDensity.current) { intersectY.toDp() } - 20.dp
                                        )
                                    )
                                }
                            }
                    }
                }

                // 채팅 입력 창
                ChatInputBar(
                    onSendChat = { message ->
                        viewModel.processIntent(LiveTrackingIntent.SendChat(message))
                    }
                )

                // 하단 참가자 상태 시트 (1/3)
                ParticipantStatusSheet(
                    participantMarkers = state.participantMarkers,
                    participantCount = state.participantCount,
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
 * 좌표를 선형 보간(lerp)하여 마커가 부드럽게 이동한다.
 */
@Composable
fun MapMarkerScope.AnimatedParticipantMarker(
    participantMarker: ParticipantMarker
) {
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

    val animatedPosition = GeoPoint(
        latitude = latAnimatable.value.toDouble(),
        longitude = lngAnimatable.value.toDouble(),
    )

    CustomMarker(
        key = participantMarker.participant.userId,
        position = animatedPosition,
        title = participantMarker.participant.nickname,
        snippet = if (participantMarker.participant.isArrived) {
            "✅ 도착"
        } else {
            "📍 ${formatDistance(participantMarker.distanceToPin)}"
        },
        // 말풍선/프로필/도착 상태가 바뀌면 마커를 다시 그리도록 키에 포함한다.
        contentKey = "${participantMarker.chatMessage}|" +
            "${participantMarker.participant.isArrived}|" +
            "${participantMarker.participant.profileImageUrl}",
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = !participantMarker.chatMessage.isNullOrEmpty(),
                // 뾱! 하고 마커 위로 튀어오르듯 팝(바운스 스프링) → 4초 뒤 뾱! 하고 쏙 사라짐.
                // 말풍선은 마커 위에 있으므로 하단 중앙(0.5, 1)을 기준점으로 스케일한다.
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ) + fadeIn(animationSpec = tween(durationMillis = 120)),
                exit = scaleOut(
                    animationSpec = tween(durationMillis = 150),
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ) + fadeOut(animationSpec = tween(durationMillis = 150)),
            ) {
                MeetPinChatBubble(
                    text = participantMarker.chatMessage ?: "",
                    isMine = false,
                )
            }
            if (!participantMarker.chatMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 커스텀 아바타 마커 (프로필 이미지는 content 슬롯으로 위임)
            MeetPinAvatar(
                initial = participantMarker.participant.nickname,
                isArrived = participantMarker.participant.isArrived,
                content = participantMarker.participant.profileImageUrl
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { url ->
                        {
                            AsyncImage(
                                model = url,
                                contentDescription = "프로필 이미지",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
            )
        }
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

/**
 * Android 시스템 공유 팝업으로 초대 딥링크를 공유한다.
 */
private fun shareInviteLink(context: Context, inviteCode: String) {
    val url = "https://meetpin.app/invite/$inviteCode"
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "MeetPin 약속 초대")
        putExtra(
            Intent.EXTRA_TEXT,
            """
            📍 MeetPin 약속 초대!

            아래 링크를 눌러 지금 위치를 공유하세요:

            $url
            """.trimIndent()
        )
    }
    context.startActivity(Intent.createChooser(sendIntent, "초대장 공유"))
}

@Composable
fun ChatInputBar(onSendChat: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MeetPinTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = "채팅을 입력하세요...",
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendChat(text)
                    text = ""
                }
            }
        ) {
            Icon(Icons.Default.Send, contentDescription = "Send Chat")
        }
    }
}
