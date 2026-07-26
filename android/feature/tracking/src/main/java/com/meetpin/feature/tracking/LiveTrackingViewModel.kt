package com.meetpin.feature.tracking

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.meetpin.core.designsystem.mvi.BaseViewModel
import com.meetpin.core.domain.repository.LocationRepository
import com.meetpin.core.domain.repository.MeetPinRepository
import com.meetpin.core.domain.usecase.CalculateEtaUseCase
import com.meetpin.core.domain.usecase.CalculateLatePenaltyUseCase
import com.meetpin.core.location.ArrivalDetector
import com.meetpin.core.model.GroupStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 실시간 트래킹 MVI ViewModel.
 *
 * - 그룹 상태와 참가자 위치를 실시간 관찰
 * - ArrivalDetector로 도착 감지
 * - 마커 좌표 보간을 위한 currentPosition/targetPosition 갱신
 * - 전원 도착 시 완료 화면으로 전환
 *
 * ETA·지각 벌칙금 계산은 :core:domain의 UseCase에 위임한다 (관심사 분리).
 */
@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val meetPinRepository: MeetPinRepository,
    private val locationRepository: LocationRepository,
    private val arrivalDetector: ArrivalDetector,
    private val calculateEta: CalculateEtaUseCase,
    private val calculateLatePenalty: CalculateLatePenaltyUseCase
) : BaseViewModel<LiveTrackingState, LiveTrackingIntent, LiveTrackingEffect>(LiveTrackingState()) {

    override fun processIntent(intent: LiveTrackingIntent) {
        when (intent) {
            is LiveTrackingIntent.StartTracking -> startTracking(intent.groupId)
            is LiveTrackingIntent.StopLocationSharing -> stopLocationSharing()
            is LiveTrackingIntent.FocusOnParticipant -> focusOnParticipant(intent.userId)
            is LiveTrackingIntent.DismissArrivalEffect -> {
                updateState { copy(showArrivalEffect = false) }
            }
            is LiveTrackingIntent.SendChat -> sendChat(intent.message)
        }
    }

    private fun startTracking(groupId: String) {
        updateState { copy(groupId = groupId, isLoading = true) }

        // 그룹 상태 + 위치 업데이트 + 지각 시간 갱신용 타이머를 동시 관찰
        combine(
            meetPinRepository.observeGroup(groupId),
            locationRepository.observeGroupLocations(groupId),
            minuteTicker()
        ) { group, locations, currentTime ->
            val pinLocation = group.pinLocation
            val pinLatLng = LatLng(pinLocation.latitude, pinLocation.longitude)

            val markers = group.participants.map { participant ->
                val locationUpdate = locations.find { it.userId == participant.userId }
                val position = locationUpdate?.let { LatLng(it.latitude, it.longitude) } ?: pinLatLng

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

                val latePenalty = calculateLatePenalty(
                    scheduledAt = group.scheduledAt,
                    now = currentTime,
                    isArrived = participant.isArrived,
                    penaltyPerMinute = group.penaltyPerMinute
                )

                ParticipantMarker(
                    participant = participant,
                    currentPosition = existingMarker?.targetPosition ?: position,
                    targetPosition = position,
                    distanceToPin = distance,
                    etaMinutes = etaMinutes,
                    chatMessage = existingMarker?.chatMessage,
                    chatTimestamp = existingMarker?.chatTimestamp,
                    lateMinutes = latePenalty.lateMinutes,
                    currentPenalty = latePenalty.amount
                )
            }

            val isAllArrived = group.participants.isNotEmpty() &&
                    group.participants.all { it.isArrived }

            // 새 도착자 감지
            val newlyArrived = markers.filter { marker ->
                marker.participant.isArrived &&
                        currentState.participantMarkers
                            .find { it.participant.userId == marker.participant.userId }
                            ?.participant?.isArrived != true
            }

            updateState {
                copy(
                    groupTitle = group.title,
                    pinLocation = pinLatLng,
                    pinPlaceName = pinLocation.placeName,
                    participantMarkers = markers,
                    isAllArrived = isAllArrived,
                    isLoading = false,
                    isLocationSharingActive = group.status == GroupStatus.ACTIVE
                )
            }

            // 도착 축하 이펙트
            newlyArrived.forEach { marker ->
                sendEffect(LiveTrackingEffect.ShowArrivalCelebration(marker.participant.nickname))
                updateState { copy(showArrivalEffect = true) }
            }

            // 전원 도착 시 완료 화면으로 이동
            if (isAllArrived || group.status == GroupStatus.FINISHED) {
                sendEffect(LiveTrackingEffect.StopLocationService)
                sendEffect(LiveTrackingEffect.NavigateToCompletion(groupId))
            }
        }
            .catch { error ->
                updateState { copy(isLoading = false) }
                sendEffect(LiveTrackingEffect.ShowError(error.message ?: "트래킹 오류"))
            }
            .launchIn(viewModelScope)
    }

    /**
     * 지각 시간 갱신용 타이머.
     *
     * 지각 표시는 분 단위이므로 1분 주기로만 방출한다.
     * (더 짧은 주기로 방출하면 참가자 마커 리스트 전체가 불필요하게 재생성되고
     *  Compose 리컴포지션도 매 tick마다 유발된다.)
     */
    private fun minuteTicker() = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(TICK_INTERVAL_MS)
        }
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
        /** 지각 시간 갱신 주기 (분 단위 표시이므로 1분) */
        const val TICK_INTERVAL_MS = 60_000L

        /** 말풍선 표시 유지 시간 */
        const val CHAT_BUBBLE_DURATION_MS = 4_000L
    }
}
