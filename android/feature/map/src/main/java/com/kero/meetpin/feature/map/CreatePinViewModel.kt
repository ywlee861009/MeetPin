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
}
