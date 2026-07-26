package com.meetpin.feature.lobby

import com.meetpin.core.designsystem.mvi.UiEffect
import com.meetpin.core.designsystem.mvi.UiIntent
import com.meetpin.core.designsystem.mvi.UiState
import com.meetpin.core.model.MeetPinGroup

/**
 * 초대 수락/거절 화면의 MVI 상태.
 */
data class InviteAcceptState(
    val inviteCode: String = "",
    val isLoading: Boolean = false,
    val group: MeetPinGroup? = null,
    val errorMessage: String? = null,
    val isAccepting: Boolean = false,
    val isDeclining: Boolean = false
) : UiState

/**
 * 초대 수락/거절 화면에서 발생하는 사용자 인텐트.
 */
sealed interface InviteAcceptIntent : UiIntent {
    data class LoadInvite(val inviteCode: String) : InviteAcceptIntent
    data object AcceptInvite : InviteAcceptIntent
    data object DeclineInvite : InviteAcceptIntent
}

/**
 * 초대 수락/거절 화면의 일회성 부수효과.
 */
sealed interface InviteAcceptEffect : UiEffect {
    data class NavigateToLobby(val groupId: String) : InviteAcceptEffect
    data object NavigateBack : InviteAcceptEffect
    data class ShowError(val message: String) : InviteAcceptEffect
}
