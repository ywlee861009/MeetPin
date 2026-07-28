package com.kero.meetpin.feature.tracking

import com.kero.meetpin.core.designsystem.mvi.UiEffect
import com.kero.meetpin.core.designsystem.mvi.UiIntent
import com.kero.meetpin.core.designsystem.mvi.UiState
import com.kero.meetpin.core.model.GeoPoint
import com.kero.meetpin.core.model.Participant

/**
 * 실시간 트래킹 화면의 참가자 마커 정보.
 */
data class ParticipantMarker(
    val participant: Participant,
    val currentPosition: GeoPoint,
    val targetPosition: GeoPoint, // 보간 애니메이션 목표 위치
    val distanceToPin: Float = 0f, // 약속 장소까지 거리 (미터)
    val etaMinutes: Int? = null, // 예상 소요시간 (분)
    val chatMessage: String? = null, // 현재 표시될 말풍선 메시지
    val chatTimestamp: Long? = null // 말풍선 표시 시작 시간
)

/**
 * 실시간 트래킹 화면의 MVI 상태.
 *
 * 도착 감지·완료 개념은 없다 — 참가자가 수락하면 지도에 표시되고 계속 공유/채팅만 한다.
 */
data class LiveTrackingState(
    val groupId: String = "",
    val groupTitle: String = "",
    val inviteCode: String = "", // 초대 링크 공유용
    val pinLocation: GeoPoint? = null,
    val pinPlaceName: String = "",
    val participantMarkers: List<ParticipantMarker> = emptyList(),
    val isLocationSharingActive: Boolean = true,
    val isLoading: Boolean = true
) : UiState {
    val participantCount: Int
        get() = participantMarkers.size
}

/**
 * 실시간 트래킹 화면의 사용자 인텐트.
 */
sealed interface LiveTrackingIntent : UiIntent {
    data class StartTracking(val groupId: String) : LiveTrackingIntent
    data object StopLocationSharing : LiveTrackingIntent
    data class FocusOnParticipant(val userId: String) : LiveTrackingIntent
    data class SendChat(val message: String) : LiveTrackingIntent

    /** [DEBUG 전용] 초대받은 상대가 수락한 상황을 단일 기기에서 시뮬레이션한다. */
    data object SimulateGuestAccept : LiveTrackingIntent
}

/**
 * 실시간 트래킹 화면의 일회성 부수효과.
 */
sealed interface LiveTrackingEffect : UiEffect {
    data class AnimateCameraToPosition(val position: GeoPoint) : LiveTrackingEffect

    /** 새 참가자가 초대를 수락해 합류했음을 알린다 (스낵바). */
    data class ShowParticipantJoined(val participantName: String) : LiveTrackingEffect

    data class ShowError(val message: String) : LiveTrackingEffect
    data object StopLocationService : LiveTrackingEffect
}
