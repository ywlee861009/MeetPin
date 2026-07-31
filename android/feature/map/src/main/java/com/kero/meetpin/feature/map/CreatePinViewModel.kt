package com.kero.meetpin.feature.map

import androidx.lifecycle.viewModelScope
import com.kero.meetpin.core.designsystem.mvi.BaseViewModel
import com.kero.meetpin.core.domain.repository.MeetPinRepository
import com.kero.meetpin.core.model.InviteStatus
import com.kero.meetpin.core.model.PinLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 핀 생성 MVI ViewModel.
 *
 * 지도 터치 → 핀 선택 → 바로 생성/공유의 단방향 흐름. 제목·일시 입력은 없다.
 */
@HiltViewModel
class CreatePinViewModel @Inject constructor(
    private val meetPinRepository: MeetPinRepository
) : BaseViewModel<PinCreateState, PinCreateIntent, PinCreateEffect>(PinCreateState()) {

    override fun processIntent(intent: PinCreateIntent) {
        when (intent) {
            is PinCreateIntent.SelectLocation -> {
                updateState { copy(selectedLocation = intent.point, errorMessage = null) }
            }
            is PinCreateIntent.SubmitPin -> {
                submitPin()
            }
            is PinCreateIntent.ClearSelection -> {
                updateState { PinCreateState() }
            }
            is PinCreateIntent.DismissError -> {
                updateState { copy(errorMessage = null) }
            }
            is PinCreateIntent.CreateDemoGroup -> {
                createDemoGroup()
            }
        }
    }

    private fun submitPin() {
        val location = currentState.selectedLocation ?: run {
            sendEffect(PinCreateEffect.ShowError("먼저 지도를 눌러 위치를 선택해주세요."))
            return
        }

        updateState { copy(isSubmitting = true) }

        viewModelScope.launch {
            val pinLocation = PinLocation(
                placeName = "",
                latitude = location.latitude,
                longitude = location.longitude
            )

            meetPinRepository.createGroup(location = pinLocation).fold(
                onSuccess = { group ->
                    updateState { copy(isSubmitting = false, createdGroupId = group.id) }
                    sendEffect(PinCreateEffect.NavigateToLiveTracking(group.id))
                },
                onFailure = { error ->
                    updateState { copy(isSubmitting = false, errorMessage = error.message) }
                    sendEffect(PinCreateEffect.ShowError(error.message ?: "약속 생성에 실패했습니다."))
                }
            )
        }
    }

    /**
     * [DEBUG 전용] 데모용 그룹을 만들어 곧장 트래킹 화면으로 진입한다.
     *
     * 새 도메인 API를 추가하지 않고 기존 인터페이스만 조합한다:
     * [MeetPinRepository.createGroup]가 호스트+친구2명(PENDING)을 시드하고,
     * [MeetPinRepository.updateInviteStatus]로 대기 중인 친구 전원을 ACCEPTED로 바꾸면
     * 그룹이 ACTIVE가 되어 트래킹 화면에 참가자 3명이 바로 표시된다.
     */
    private fun createDemoGroup() {
        updateState { copy(isSubmitting = true) }

        viewModelScope.launch {
            val demoLocation = PinLocation(
                placeName = "",
                latitude = DEMO_PIN_LATITUDE,
                longitude = DEMO_PIN_LONGITUDE
            )

            meetPinRepository.createGroup(location = demoLocation).fold(
                onSuccess = { group ->
                    // 시드된 친구(PENDING) 전원 승낙 처리 → 그룹 ACTIVE.
                    meetPinRepository.updateInviteStatus(group.id, InviteStatus.ACCEPTED)
                    updateState { copy(isSubmitting = false, createdGroupId = group.id) }
                    sendEffect(PinCreateEffect.NavigateToLiveTracking(group.id))
                },
                onFailure = { error ->
                    updateState { copy(isSubmitting = false, errorMessage = error.message) }
                    sendEffect(PinCreateEffect.ShowError(error.message ?: "데모 그룹 생성에 실패했습니다."))
                }
            )
        }
    }

    private companion object {
        /** [데모] 기본 약속 장소 — 서울 시청 부근 (CreatePinScreen 기본 카메라 위치와 동일). */
        const val DEMO_PIN_LATITUDE = 37.5666805
        const val DEMO_PIN_LONGITUDE = 126.9784147
    }
}
