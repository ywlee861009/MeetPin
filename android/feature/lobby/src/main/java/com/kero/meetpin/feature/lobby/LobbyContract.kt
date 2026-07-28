package com.kero.meetpin.feature.lobby

import com.kero.meetpin.core.designsystem.mvi.UiEffect
import com.kero.meetpin.core.designsystem.mvi.UiIntent
import com.kero.meetpin.core.designsystem.mvi.UiState
import com.kero.meetpin.core.model.GroupStatus
import com.kero.meetpin.core.model.Participant

/**
 * 대기실(Lobby) 화면의 MVI 상태.
 */
data class LobbyState(
    val groupId: String = "",
    val groupTitle: String = "",
    val participants: List<Participant> = emptyList(),
    val isAllAccepted: Boolean = false,
    val groupStatus: GroupStatus = GroupStatus.LOBBY,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) : UiState {
    val acceptedCount: Int
        get() = participants.count { it.inviteStatus == com.kero.meetpin.core.model.InviteStatus.ACCEPTED }

    val totalCount: Int
        get() = participants.size

    val progressRatio: Float
        get() = if (totalCount > 0) acceptedCount.toFloat() / totalCount else 0f
}

/**
 * 대기실 화면의 사용자 인텐트.
 */
sealed interface LobbyIntent : UiIntent {
    data class ObserveGroup(val groupId: String) : LobbyIntent
}

/**
 * 대기실 화면의 일회성 부수효과.
 */
sealed interface LobbyEffect : UiEffect {
    data class NavigateToLiveTracking(val groupId: String) : LobbyEffect
    data class ShowError(val message: String) : LobbyEffect
}
