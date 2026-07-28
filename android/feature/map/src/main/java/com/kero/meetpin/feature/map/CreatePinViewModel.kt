package com.kero.meetpin.feature.map

import androidx.lifecycle.viewModelScope
import com.kero.meetpin.core.designsystem.mvi.BaseViewModel
import com.kero.meetpin.core.domain.repository.MeetPinRepository
import com.kero.meetpin.core.model.PinLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 핀 생성 MVI ViewModel.
 *
 * 지도 터치 → 핀 선택 → 약속 정보 입력 → 제출의 단방향 데이터 흐름을 관리.
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
            is PinCreateIntent.UpdateTitle -> {
                updateState { copy(title = intent.title) }
            }
            is PinCreateIntent.UpdateDate -> {
                updateState { copy(scheduledDate = intent.dateMillis) }
            }
            is PinCreateIntent.UpdateTime -> {
                updateState { copy(scheduledTime = intent.timeMillis) }
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
        }
    }

    private fun submitPin() {
        val state = currentState
        if (!state.isFormValid) {
            sendEffect(PinCreateEffect.ShowError("모든 필드를 입력해주세요."))
            return
        }

        val location = state.selectedLocation ?: return
        val scheduledAt = state.scheduledAt ?: return

        updateState { copy(isSubmitting = true) }

        viewModelScope.launch {
            val pinLocation = PinLocation(
                placeName = state.title,
                latitude = location.latitude,
                longitude = location.longitude
            )

            meetPinRepository.createGroup(
                title = state.title,
                location = pinLocation,
                scheduledAt = scheduledAt
            ).fold(
                onSuccess = { group ->
                    updateState { copy(isSubmitting = false, createdGroupId = group.id) }
                    sendEffect(
                        PinCreateEffect.NavigateToInviteShare(
                            groupId = group.id,
                            groupTitle = group.title,
                            inviteCode = group.inviteCode
                        )
                    )
                },
                onFailure = { error ->
                    updateState { copy(isSubmitting = false, errorMessage = error.message) }
                    sendEffect(PinCreateEffect.ShowError(error.message ?: "약속 생성에 실패했습니다."))
                }
            )
        }
    }
}
