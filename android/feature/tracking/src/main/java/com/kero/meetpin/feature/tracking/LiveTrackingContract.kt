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
 * 참가자가 수락하면 지도에 표시되고 계속 공유/채팅한다.
 * 도착 감지 시 참가자의 도착 상태(체크마크·"도착 완료!" 라벨)만 켜지며, 완료 화면·자동 종료는 없다.
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

    /**
     * [DEBUG 전용] 친구가 채팅을 보낸 상황을 시뮬레이션한다.
     * [friendIndex]는 비호스트(친구) 참가자의 순번(0=첫 친구, 1=둘째 친구…).
     * userId 대신 순번을 쓰므로 UI가 :core:data의 참가자 id를 알 필요가 없다.
     */
    data class SimulateGuestChat(val friendIndex: Int) : LiveTrackingIntent

    /**
     * [DEBUG 전용] 친구들이 각자 위치에서 약속 장소로 이동해 도착하는 상황을 시뮬레이션한다.
     * 누르면 친구 마커가 목적지 핀 쪽으로 보간 이동하고, 반경 진입 시 도착 상태로 전환된다.
     */
    data object SimulateFriendsDeparture : LiveTrackingIntent

    /**
     * [DEBUG 전용] 출발 알림 주기(최소 15분)를 기다리지 않고 출발 판정을 즉시 1회 실행한다.
     * 실제 [com.kero.meetpin.core.location.DepartureCheckWorker] 경로(위치→ETA→판정→알림)를
     * 그대로 태워 e2e 확인용으로 쓴다.
     */
    data object TriggerDepartureCheckNow : LiveTrackingIntent
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
