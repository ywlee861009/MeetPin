package com.meetpin.feature.tracking

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.meetpin.core.designsystem.mvi.BaseViewModel
import com.meetpin.core.domain.repository.LocationRepository
import com.meetpin.core.domain.repository.MeetPinRepository
import com.meetpin.core.location.ArrivalDetector
import com.meetpin.core.model.GroupStatus
import com.meetpin.core.model.PinLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * 실시간 트래킹 MVI ViewModel.
 *
 * - 그룹 상태와 참가자 위치를 실시간 관찰
 * - ArrivalDetector로 도착 감지
 * - 마커 좌표 보간을 위한 currentPosition/targetPosition 갱신
 * - 전원 도착 시 완료 화면으로 전환
 */
@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val meetPinRepository: MeetPinRepository,
    private val locationRepository: LocationRepository,
    private val arrivalDetector: ArrivalDetector
) : BaseViewModel<LiveTrackingState, LiveTrackingIntent, LiveTrackingEffect>(LiveTrackingState()) {

    override fun processIntent(intent: LiveTrackingIntent) {
        when (intent) {
            is LiveTrackingIntent.StartTracking -> startTracking(intent.groupId)
            is LiveTrackingIntent.StopLocationSharing -> stopLocationSharing()
            is LiveTrackingIntent.FocusOnParticipant -> focusOnParticipant(intent.userId)
            is LiveTrackingIntent.DismissArrivalEffect -> {
                updateState { copy(showArrivalEffect = false) }
            }
        }
    }

    private fun startTracking(groupId: String) {
        updateState { copy(groupId = groupId, isLoading = true) }

        // 그룹 상태 + 위치 업데이트를 동시 관찰
        combine(
            meetPinRepository.observeGroup(groupId),
            locationRepository.observeGroupLocations(groupId)
        ) { group, locations ->
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

                // 간이 ETA 계산 (도보 평균 속도 5km/h 기준)
                val etaMinutes = if (distance > 0) {
                    val walkingSpeedMps = 5000f / 3600f // 약 1.39 m/s
                    (distance / walkingSpeedMps / 60f).toInt().coerceAtLeast(1)
                } else null

                ParticipantMarker(
                    participant = participant,
                    currentPosition = existingMarker?.targetPosition ?: position,
                    targetPosition = position,
                    distanceToPin = distance,
                    etaMinutes = etaMinutes
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
                sendEffect(LiveTrackingEffect.NavigateToCompletion(groupId))
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
        // TODO: LocationTrackingService.stop() 호출 및 서버에 종료 알림
    }

    private fun focusOnParticipant(userId: String) {
        val marker = currentState.participantMarkers.find { it.participant.userId == userId }
        marker?.let {
            sendEffect(LiveTrackingEffect.AnimateCameraToPosition(it.targetPosition))
        }
    }
}
