package com.meetpin.feature.lobby

import androidx.lifecycle.viewModelScope
import com.meetpin.core.designsystem.mvi.BaseViewModel
import com.meetpin.core.domain.repository.MeetPinRepository
import com.meetpin.core.model.InviteStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 초대 수락/거절 MVI ViewModel.
 *
 * 딥링크 파싱 → 초대 코드로 그룹 조회 → 수락/거절 처리.
 */
@HiltViewModel
class InviteAcceptViewModel @Inject constructor(
    private val meetPinRepository: MeetPinRepository
) : BaseViewModel<InviteAcceptState, InviteAcceptIntent, InviteAcceptEffect>(InviteAcceptState()) {

    override fun processIntent(intent: InviteAcceptIntent) {
        when (intent) {
            is InviteAcceptIntent.LoadInvite -> loadGroupByInviteCode(intent.inviteCode)
            is InviteAcceptIntent.AcceptInvite -> acceptInvite()
            is InviteAcceptIntent.DeclineInvite -> declineInvite()
        }
    }

    private fun loadGroupByInviteCode(inviteCode: String) {
        updateState { copy(inviteCode = inviteCode, isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            meetPinRepository.getGroupByInviteCode(inviteCode).fold(
                onSuccess = { group ->
                    updateState { copy(isLoading = false, group = group) }
                },
                onFailure = { error ->
                    updateState { copy(isLoading = false, errorMessage = error.message) }
                    sendEffect(InviteAcceptEffect.ShowError(error.message ?: "초대 정보를 불러올 수 없습니다."))
                }
            )
        }
    }

    private fun acceptInvite() {
        val groupId = currentState.group?.id ?: return
        updateState { copy(isAccepting = true) }

        viewModelScope.launch {
            meetPinRepository.updateInviteStatus(groupId, InviteStatus.ACCEPTED).fold(
                onSuccess = {
                    updateState { copy(isAccepting = false) }
                    sendEffect(InviteAcceptEffect.NavigateToLobby(groupId))
                },
                onFailure = { error ->
                    updateState { copy(isAccepting = false) }
                    sendEffect(InviteAcceptEffect.ShowError(error.message ?: "승낙 처리에 실패했습니다."))
                }
            )
        }
    }

    private fun declineInvite() {
        val groupId = currentState.group?.id ?: return
        updateState { copy(isDeclining = true) }

        viewModelScope.launch {
            meetPinRepository.updateInviteStatus(groupId, InviteStatus.DECLINED).fold(
                onSuccess = {
                    updateState { copy(isDeclining = false) }
                    sendEffect(InviteAcceptEffect.NavigateBack)
                },
                onFailure = { error ->
                    updateState { copy(isDeclining = false) }
                    sendEffect(InviteAcceptEffect.ShowError(error.message ?: "거절 처리에 실패했습니다."))
                }
            )
        }
    }
}
