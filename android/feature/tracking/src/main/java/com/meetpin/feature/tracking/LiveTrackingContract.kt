package com.meetpin.feature.tracking

import com.google.android.gms.maps.model.LatLng
import com.meetpin.core.designsystem.mvi.UiEffect
import com.meetpin.core.designsystem.mvi.UiIntent
import com.meetpin.core.designsystem.mvi.UiState
import com.meetpin.core.model.LocationUpdate
import com.meetpin.core.model.Participant

/**
 * 실시간 트래킹 화면의 참가자 마커 정보.
 */
data class ParticipantMarker(
    val participant: Participant,
    val currentPosition: LatLng,
    val targetPosition: LatLng, // 보간 애니메이션 목표 위치
    val distanceToPin: Float = 0f, // 약속 장소까지 거리 (미터)
    val etaMinutes: Int? = null // 예상 소요시간 (분)
)

/**
 * 실시간 트래킹 화면의 MVI 상태.
 */
data class LiveTrackingState(
    val groupId: String = "",
    val groupTitle: String = "",
    val pinLocation: LatLng? = null,
    val pinPlaceName: String = "",
    val participantMarkers: List<ParticipantMarker> = emptyList(),
    val myArrivedStatus: Boolean = false,
    val isAllArrived: Boolean = false,
    val isLocationSharingActive: Boolean = true,
    val showArrivalEffect: Boolean = false,
    val isLoading: Boolean = true
) : UiState {
    val arrivedCount: Int
        get() = participantMarkers.count { it.participant.isArrived }

    val totalCount: Int
        get() = participantMarkers.size
}

/**
 * 실시간 트래킹 화면의 사용자 인텐트.
 */
sealed interface LiveTrackingIntent : UiIntent {
    data class StartTracking(val groupId: String) : LiveTrackingIntent
    data object StopLocationSharing : LiveTrackingIntent
    data class FocusOnParticipant(val userId: String) : LiveTrackingIntent
    data object DismissArrivalEffect : LiveTrackingIntent
}

/**
 * 실시간 트래킹 화면의 일회성 부수효과.
 */
sealed interface LiveTrackingEffect : UiEffect {
    data class AnimateCameraToPosition(val position: LatLng) : LiveTrackingEffect
    data class ShowArrivalCelebration(val participantName: String) : LiveTrackingEffect
    data class NavigateToCompletion(val groupId: String) : LiveTrackingEffect
    data class ShowError(val message: String) : LiveTrackingEffect
    data object StopLocationService : LiveTrackingEffect
}
