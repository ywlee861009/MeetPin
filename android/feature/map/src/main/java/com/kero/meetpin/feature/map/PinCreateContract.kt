package com.kero.meetpin.feature.map

import com.kero.meetpin.core.designsystem.mvi.UiEffect
import com.kero.meetpin.core.designsystem.mvi.UiIntent
import com.kero.meetpin.core.designsystem.mvi.UiState
import com.kero.meetpin.core.model.GeoPoint

/**
 * 핀 생성 화면의 MVI 상태.
 *
 * 제목·일시 입력을 없앤 즉시 공유 플로우 — 위치만 찍으면 바로 만들 수 있다.
 */
data class PinCreateState(
    val selectedLocation: GeoPoint? = null,
    val isSubmitting: Boolean = false,
    val createdGroupId: String? = null,
    val errorMessage: String? = null
) : UiState {
    val isFormValid: Boolean
        get() = selectedLocation != null
}

/**
 * 핀 생성 화면에서 발생하는 사용자 인텐트.
 */
sealed interface PinCreateIntent : UiIntent {
    data class SelectLocation(val point: GeoPoint) : PinCreateIntent
    data object SubmitPin : PinCreateIntent
    data object ClearSelection : PinCreateIntent
    data object DismissError : PinCreateIntent

    /**
     * [DEBUG 전용] 핀 생성 → 대기실 → 승낙 단계를 건너뛰고,
     * 전원 승낙된 데모 그룹을 즉시 만들어 실시간 지도로 진입한다.
     */
    data object CreateDemoGroup : PinCreateIntent
}

/**
 * 핀 생성 화면의 일회성 부수효과.
 */
sealed interface PinCreateEffect : UiEffect {
    /** 약속 생성 완료 → 바로 실시간 지도(트래킹)로 이동한다. (대기실 없음) */
    data class NavigateToLiveTracking(val groupId: String) : PinCreateEffect

    data class ShowError(val message: String) : PinCreateEffect
}
