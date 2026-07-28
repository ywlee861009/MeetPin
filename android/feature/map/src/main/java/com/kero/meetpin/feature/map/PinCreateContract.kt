package com.kero.meetpin.feature.map

import com.kero.meetpin.core.designsystem.mvi.UiEffect
import com.kero.meetpin.core.designsystem.mvi.UiIntent
import com.kero.meetpin.core.designsystem.mvi.UiState
import com.kero.meetpin.core.model.GeoPoint

/**
 * 핀 생성 화면의 MVI 상태.
 */
data class PinCreateState(
    val selectedLocation: GeoPoint? = null,
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
    data class SelectLocation(val point: GeoPoint) : PinCreateIntent
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
    /**
     * 초대 공유 화면으로 이동.
     *
     * [groupTitle]은 `InviteShareScreen`이 요약 카드와 공유 문구에 쓰는 값이다.
     * 해당 화면은 ViewModel이 없는 stateless 화면이라 제목을 스스로 조회할 수 없으므로
     * 여기서 함께 실어 보낸다.
     */
    data class NavigateToInviteShare(
        val groupId: String,
        val groupTitle: String,
        val inviteCode: String
    ) : PinCreateEffect

    data class ShowError(val message: String) : PinCreateEffect
}
