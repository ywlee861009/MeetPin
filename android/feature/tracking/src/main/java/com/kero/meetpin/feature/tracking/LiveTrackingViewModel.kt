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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 실시간 트래킹 MVI ViewModel.
 *
 * - 그룹 상태와 참가자 위치를 실시간 관찰
 * - 참가자가 초대를 수락하면 스낵바로 알리고 지도에 마커로 표시
 * - 마커 좌표 보간을 위한 currentPosition/targetPosition 갱신
 *
 * 도착 감지는 하되(반경 진입 시 도착 시각화만 켠다), 완료 화면·자동 종료 개념은 없다.
 * 도착 후에도 계속 위치 공유/채팅을 이어간다.
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
            is LiveTrackingIntent.SimulateGuestChat -> simulateGuestChat(intent.friendIndex)
            is LiveTrackingIntent.SimulateFriendsDeparture -> simulateFriendsDeparture()
        }
    }

    /**
     * 트래킹 관찰 코루틴. 재진입 시 이전 것을 반드시 취소한다.
     * (화면 재구성으로 StartTracking이 다시 들어와도 collector가 누적되지 않도록.)
     */
    private var trackingJob: Job? = null

    /**
     * 이미 도착 보고를 보낸 참가자 userId 집합.
     * 위치 emission이 반경 안에서 여러 번 들어와도 [MeetPinRepository.reportArrival]가
     * 중복 호출되지 않도록 막는다. (리포지토리도 멱등이지만 불필요한 호출 자체를 줄인다.)
     */
    private val reportedArrivals = mutableSetOf<String>()

    /**
     * [데모] 친구 이동 진행도(0f=출발지, 1f=목적지). [simulateFriendsDeparture]가 시간에 따라
     * 0→1로 올리면, 친구 마커 좌표가 출발지→핀으로 보간되어 이동·도착이 재생된다.
     * 데모 이동을 트리거하기 전에는 0f로 고정되어 친구가 각자 위치(광화문/강남)에 머문다.
     */
    private val demoDepartureProgress = MutableStateFlow(0f)

    /** [데모] 이동 진행도를 올리는 코루틴. 재진입/재트리거 시 이전 것을 취소한다. */
    private var demoDepartureJob: Job? = null

    private fun startTracking(groupId: String) {
        updateState { copy(groupId = groupId, isLoading = true) }

        trackingJob?.cancel()
        demoDepartureJob?.cancel()
        demoDepartureProgress.value = 0f
        reportedArrivals.clear()

        // 그룹 상태 + 내 실시간 GPS + 데모 이동 진행도를 동시 관찰.
        // 내 위치는 항상 실제 GPS를 쓰되, 아직 안 들어왔거나 권한이 없으면 핀으로 폴백한다.
        trackingJob = combine(
            meetPinRepository.observeGroup(groupId),
            locationRepository.getCurrentLocation()
                .map<_, GeoPoint?> { GeoPoint(it.latitude, it.longitude) }
                .onStart { emit(null) }
                .catch { emit(null) },
            demoDepartureProgress
        ) { group, myLocation, departureProgress ->
            currentHostId = group.hostId
            val pinLocation = group.pinLocation
            val pinLatLng = GeoPoint(pinLocation.latitude, pinLocation.longitude)

            // 수락(ACCEPTED)한 참가자만 지도·목록에 표시한다. (대기 중인 초대는 숨김)
            val accepted = group.participants
                .filter { it.inviteStatus == InviteStatus.ACCEPTED }

            // [데모] 비호스트(친구)에게 순번(index)을 매겨 고정 좌표를 배정한다.
            // userId를 키로 쓰지 않으므로 :core:data의 참가자 id 상수를 알 필요가 없다.
            val friendIndexByUserId = accepted
                .filter { it.userId != group.hostId }
                .mapIndexed { index, participant -> participant.userId to index }
                .toMap()

            val markers = accepted.map { participant ->
                val isHost = participant.userId == group.hostId
                // 호스트(나) = 실제 GPS(없으면 핀), 친구 = 순번별 데모 좌표(광화문/강남…).
                // 친구는 데모 이동 진행도(departureProgress)에 따라 출발지→핀으로 보간 이동한다.
                val position = when {
                    isHost -> myLocation ?: pinLatLng
                    else -> {
                        val friendIndex = friendIndexByUserId[participant.userId]
                        friendIndex?.let { index ->
                            lerp(demoFriendLocation(index), pinLatLng, departureProgress)
                        } ?: pinLatLng
                    }
                }

                // 기존 마커 정보 가져와서 currentPosition 유지 (애니메이션 보간용)
                val existingMarker = currentState.participantMarkers
                    .find { it.participant.userId == participant.userId }

                val distance = arrivalDetector.calculateDistance(
                    position.latitude, position.longitude,
                    pinLocation.latitude, pinLocation.longitude
                )

                // 도착 감지: 반경 내 진입 시 리포지토리에 보고해 도착 시각화(체크마크·라벨)를 켠다.
                // 호스트는 실제 GPS가 들어온 경우에만 판정한다. GPS 미수신 시 position이 핀으로
                // 폴백(distance=0)되어 즉시 도착으로 오판되는 것을 막기 위함이다.
                val hasRealPosition = if (isHost) myLocation != null else true
                if (hasRealPosition &&
                    !participant.isArrived &&
                    participant.userId !in reportedArrivals &&
                    arrivalDetector.isWithinRadius(position.latitude, position.longitude, pinLocation)
                ) {
                    reportedArrivals += participant.userId
                    viewModelScope.launch {
                        meetPinRepository.reportArrival(groupId, participant.userId)
                    }
                }

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

    /**
     * [DEBUG 전용] 친구가 채팅을 보낸 상황을 재현한다.
     * [friendIndex]로 비호스트(친구) 순번을 지정해 해당 마커에 캔드 메시지를 순환 표시한다.
     */
    private fun simulateGuestChat(friendIndex: Int) {
        val friend = currentState.participantMarkers
            .map { it.participant }
            .filter { it.userId != currentHostId }
            .getOrNull(friendIndex) ?: return

        val message = CANNED_GUEST_CHATS[chatRotation % CANNED_GUEST_CHATS.size]
        chatRotation++
        showChatBubble(friend.userId, message)
    }

    /** 합류/채팅 시뮬레이션에서 '친구'를 식별하기 위한 호스트 id 캐시. */
    private var currentHostId: String = ""

    /** 시뮬 채팅에서 캔드 메시지를 돌려쓰기 위한 회전 인덱스. */
    private var chatRotation: Int = 0

    private fun sendChat(message: String) {
        // TODO: 실제 앱에서는 서버로 채팅을 전송하고, 서버에서 받아서 업데이트해야 합니다.
        // 현재는 로컬에서 내(호스트) 마커에 바로 표시되도록 모의(Mock) 구현합니다.
        val myUserId = currentHostId.ifBlank {
            currentState.participantMarkers.firstOrNull()?.participant?.userId
        } ?: return
        showChatBubble(myUserId, message)
    }

    /**
     * 지정한 참가자 마커에 말풍선을 띄우고 [CHAT_BUBBLE_DURATION_MS] 후 자동으로 지운다.
     * 자동 닫힘은 그 사이 새 말풍선(다른 timestamp)으로 덮이지 않은 경우에만 동작한다.
     */
    private fun showChatBubble(userId: String, message: String) {
        val timestamp = System.currentTimeMillis()
        updateState {
            copy(participantMarkers = participantMarkers.map { marker ->
                if (marker.participant.userId == userId) {
                    marker.copy(chatMessage = message, chatTimestamp = timestamp)
                } else {
                    marker
                }
            })
        }

        viewModelScope.launch {
            delay(CHAT_BUBBLE_DURATION_MS)
            updateState {
                copy(participantMarkers = participantMarkers.map { marker ->
                    if (marker.participant.userId == userId && marker.chatTimestamp == timestamp) {
                        marker.copy(chatMessage = null, chatTimestamp = null)
                    } else {
                        marker
                    }
                })
            }
        }
    }

    /**
     * [DEBUG 전용] 친구들이 약속 장소로 출발해 도착하는 상황을 재현한다.
     * 진행도를 [DEMO_TRAVEL_STEPS]단계에 걸쳐 0→1로 올리면, combine이 매 단계 재방출되어
     * 친구 마커가 목적지 핀 쪽으로 보간 이동한다. 반경(50m) 진입 시 기존 도착 감지 경로가
     * 발동해 [MeetPinRepository.reportArrival] → 체크마크·"도착 완료!" 라벨이 켜진다.
     */
    private fun simulateFriendsDeparture() {
        demoDepartureJob?.cancel()
        demoDepartureJob = viewModelScope.launch {
            for (step in 1..DEMO_TRAVEL_STEPS) {
                delay(DEMO_TRAVEL_STEP_MS)
                demoDepartureProgress.value = step.toFloat() / DEMO_TRAVEL_STEPS
            }
        }
    }

    /**
     * [데모] 친구 순번(index)에 대응하는 고정 출발 좌표를 돌려준다.
     * 좌표 개수보다 친구가 많으면 순환시켜 마커가 핀에 겹치지 않게 한다.
     */
    private fun demoFriendLocation(friendIndex: Int): GeoPoint =
        DEMO_FRIEND_LOCATIONS[friendIndex % DEMO_FRIEND_LOCATIONS.size]

    /** [데모] 두 좌표를 [t](0f..1f)로 선형 보간한다. 데모 이동 경로 계산용. */
    private fun lerp(from: GeoPoint, to: GeoPoint, t: Float): GeoPoint =
        GeoPoint(
            latitude = from.latitude + (to.latitude - from.latitude) * t,
            longitude = from.longitude + (to.longitude - from.longitude) * t
        )

    private companion object {
        /** 말풍선 표시 유지 시간 */
        const val CHAT_BUBBLE_DURATION_MS = 4_000L

        /** [데모] 친구 이동 시뮬레이션 단계 수. 각 단계마다 마커가 1초 tween으로 보간 이동한다. */
        const val DEMO_TRAVEL_STEPS = 12

        /**
         * [데모] 이동 단계 간 간격. 마커 tween(1초)보다 약간 길게 두어 끊김 없이 이어지게 한다.
         * (총 이동 시간 ≈ DEMO_TRAVEL_STEPS × 이 값 = 약 14초)
         */
        const val DEMO_TRAVEL_STEP_MS = 1_200L

        /** [데모] 친구 위치 — 참가자 순번대로 배정. 0=광화문 광장, 1=강남역. */
        val DEMO_FRIEND_LOCATIONS = listOf(
            GeoPoint(37.5759, 126.9769), // 광화문 광장
            GeoPoint(37.4979, 127.0276)  // 강남역
        )

        /** [데모] 시뮬 채팅에서 돌려쓰는 친구 캔드 메시지. */
        val CANNED_GUEST_CHATS = listOf(
            "거의 다 왔어! 🏃",
            "5분 뒤 도착~",
            "커피 사갈까? ☕",
            "먼저 자리 잡을게"
        )
    }
}
