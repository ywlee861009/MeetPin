package com.meetpin.feature.map

import com.google.android.gms.maps.model.LatLng
import com.meetpin.core.designsystem.mvi.UiEffect
import com.meetpin.core.designsystem.mvi.UiIntent
import com.meetpin.core.designsystem.mvi.UiState

/**
 * 핀 생성 화면의 MVI 상태.
 */
data class PinCreateState(
    val selectedLocation: LatLng? = null,
    val title: String = "",
    val scheduledDate: Long? = null,
    val scheduledTime: Long? = null,
    val isSubmitting: Boolean = false,
    val createdGroupId: String? = null,
    val errorMessage: String? = null
) : UiState {
    val isFormValid: Boolean
        get() = selectedLocation != null &&
                title.isNotBlank() &&
                scheduledDate != null &&
                scheduledTime != null

    val scheduledAt: Long?
        get() {
            val date = scheduledDate ?: return null
            val time = scheduledTime ?: return null
            return date + time
        }
}

/**
 * 핀 생성 화면에서 발생하는 사용자 인텐트.
 */
sealed interface PinCreateIntent : UiIntent {
    data class SelectLocation(val latLng: LatLng) : PinCreateIntent
    data class UpdateTitle(val title: String) : PinCreateIntent
    data class UpdateDate(val dateMillis: Long) : PinCreateIntent
    data class UpdateTime(val timeMillis: Long) : PinCreateIntent
    data object SubmitPin : PinCreateIntent
    data object ClearSelection : PinCreateIntent
    data object DismissError : PinCreateIntent
}

/**
 * 핀 생성 화면의 일회성 부수효과.
 */
sealed interface PinCreateEffect : UiEffect {
    data class NavigateToInviteShare(val groupId: String, val inviteCode: String) : PinCreateEffect
    data class ShowError(val message: String) : PinCreateEffect
}
