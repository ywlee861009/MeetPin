package com.kero.meetpin.feature.tracking

import androidx.lifecycle.viewModelScope
import com.kero.meetpin.core.designsystem.mvi.BaseViewModel
import com.kero.meetpin.core.domain.repository.LocationRepository
import com.kero.meetpin.core.domain.repository.MeetPinRepository
import com.kero.meetpin.core.domain.usecase.CalculateEtaUseCase
import com.kero.meetpin.core.location.ArrivalDetector
import com.kero.meetpin.core.model.GeoPoint
import com.kero.meetpin.core.model.InviteStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 실시간 트래킹 MVI ViewModel.
 *
 * - 그룹 상태와 참가자 위치를 실시간 관찰
 * - 참가자가 초대를 수락하면 스낵바로 알리고 지도에 마커로 표시
 * - 마커 좌표 보간을 위한 currentPosition/targetPosition 갱신
 *
 * 도착 감지·완료(자동 종료) 개념은 없다. 계속 위치 공유/채팅만 한다.
 * ETA·거리 계산은 :core:domain / :core:location에 위임한다 (관심사 분리).
 */
@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val meetPinRepository: MeetPinRepository,
    private val locationRepository: LocationRepository,
    private val arrivalDetector: ArrivalDetector,
    private val calculateEta: CalculateEtaUseCase
) : BaseViewModel<LiveTrackingState, LiveTrackingIntent, LiveTrackingEffect>(LiveTrackingState()) {

    override fun processIntent(intent: LiveTrackingIntent) {
        when (intent) {
            is LiveTrackingIntent.StartTracking -> startTracking(intent.groupId)
            is LiveTrackingIntent.StopLocationSharing -> stopLocationSharing()
            is LiveTrackingIntent.FocusOnParticipant -> focusOnParticipant(intent.userId)
            is LiveTrackingIntent.SendChat -> sendChat(intent.message)
            is LiveTrackingIntent.SimulateGuestAccept -> simulateGuestAccept()
        }
    }

    /**
     * 트래킹 관찰 코루틴. 재진입 시 이전 것을 반드시 취소한다.
     * (화면 재구성으로 StartTracking이 다시 들어와도 collector가 누적되지 않도록.)
     */
    private var trackingJob: Job? = null

    private fun startTracking(groupId: String) {
        updateState { copy(groupId = groupId, isLoading = true) }

        trackingJob?.cancel()

        // 그룹 상태 + 위치 업데이트를 동시 관찰
        trackingJob = combine(
            meetPinRepository.observeGroup(groupId),
            locationRepository.observeGroupLocations(groupId)
        ) { group, locations ->
            val pinLocation = group.pinLocation
            val pinLatLng = GeoPoint(pinLocation.latitude, pinLocation.longitude)

            // 수락(ACCEPTED)한 참가자만 지도·목록에 표시한다. (대기 중인 초대는 숨김)
            val markers = group.participants
                .filter { it.inviteStatus == InviteStatus.ACCEPTED }
                .map { participant ->
                val locationUpdate = locations.find { it.userId == participant.userId }
                val position = locationUpdate?.let { GeoPoint(it.latitude, it.longitude) } ?: pinLatLng

                // 기존 마커 정보 가져와서 currentPosition 유지 (애니메이션 보간용)
                val existingMarker = currentState.participantMarkers
                    .find { it.participant.userId == participant.userId }

                val distance = locationUpdate?.let {
                    arrivalDetector.calculateDistance(
                        it.latitude, it.longitude,
                        pinLocation.latitude, pinLocation.longitude
                    )
                } ?: 0f

                val etaMinutes = calculateEta(distanceMeters = distance)

                ParticipantMarker(
                    participant = participant,
                    currentPosition = existingMarker?.targetPosition ?: position,
                    targetPosition = position,
                    distanceToPin = distance,
                    etaMinutes = etaMinutes,
                    chatMessage = existingMarker?.chatMessage,
                    chatTimestamp = existingMarker?.chatTimestamp
                )
            }

            // 새로 수락(합류)한 참가자 감지: 호스트 제외, 직전에 ACCEPTED가 아니었던 참가자
            val newlyJoined = group.participants.filter { participant ->
                participant.userId != group.hostId &&
                    participant.inviteStatus == InviteStatus.ACCEPTED &&
                    currentState.participantMarkers
                        .find { it.participant.userId == participant.userId }
                        ?.participant?.inviteStatus != InviteStatus.ACCEPTED
            }

            updateState {
                copy(
                    groupTitle = group.title,
                    inviteCode = group.inviteCode,
                    pinLocation = pinLatLng,
                    pinPlaceName = pinLocation.placeName,
                    participantMarkers = markers,
                    isLoading = false
                )
            }

            // 합류 알림 스낵바
            newlyJoined.forEach { participant ->
                sendEffect(LiveTrackingEffect.ShowParticipantJoined(participant.nickname))
            }
        }
            .catch { error ->
                updateState { copy(isLoading = false) }
                sendEffect(LiveTrackingEffect.ShowError(error.message ?: "트래킹 오류"))
            }
            .launchIn(viewModelScope)
    }

    private fun stopLocationSharing() {
        updateState { copy(isLocationSharingActive = false) }
        sendEffect(LiveTrackingEffect.StopLocationService)

        // TODO: 서버에 종료 알림 (API 필요)
    }

    private fun focusOnParticipant(userId: String) {
        val marker = currentState.participantMarkers.find { it.participant.userId == userId }
        marker?.let {
            sendEffect(LiveTrackingEffect.AnimateCameraToPosition(it.targetPosition))
        }
    }

    /**
     * [DEBUG 전용] 초대받은 상대가 수락한 상황을 단일 기기에서 재현한다.
     * PENDING 참가자를 ACCEPTED로 바꾸면 observeGroup 흐름을 타고 합류 스낵바가 뜬다.
     */
    private fun simulateGuestAccept() {
        val groupId = currentState.groupId.ifBlank { return }
        viewModelScope.launch {
            meetPinRepository.updateInviteStatus(groupId, InviteStatus.ACCEPTED)
        }
    }

    private fun sendChat(message: String) {
        // TODO: 실제 앱에서는 서버로 채팅을 전송하고, 서버에서 받아서 업데이트해야 합니다.
        // 현재는 로컬에서 내(첫 번째) 마커에 바로 표시되도록 모의(Mock) 구현합니다.
        val updatedMarkers = currentState.participantMarkers.mapIndexed { index, marker ->
            if (index == 0) { // 임시로 첫 번째 유저를 본인으로 가정
                marker.copy(
                    chatMessage = message,
                    chatTimestamp = System.currentTimeMillis()
                )
            } else {
                marker
            }
        }
        updateState { copy(participantMarkers = updatedMarkers) }

        // 말풍선 자동 닫기
        viewModelScope.launch {
            delay(CHAT_BUBBLE_DURATION_MS)
            updateState {
                copy(participantMarkers = currentState.participantMarkers.mapIndexed { index, marker ->
                    if (index == 0) marker.copy(chatMessage = null, chatTimestamp = null)
                    else marker
                })
            }
        }
    }

    private companion object {
        /** 말풍선 표시 유지 시간 */
        const val CHAT_BUBBLE_DURATION_MS = 4_000L
    }
}
