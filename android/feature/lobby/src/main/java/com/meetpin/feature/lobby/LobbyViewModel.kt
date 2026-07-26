package com.meetpin.feature.lobby

import androidx.lifecycle.viewModelScope
import com.meetpin.core.designsystem.mvi.BaseViewModel
import com.meetpin.core.domain.repository.MeetPinRepository
import com.meetpin.core.model.GroupStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * 대기실 MVI ViewModel.
 *
 * 그룹 상태를 실시간 관찰하여 참가자 승낙 현황을 업데이트하고,
 * 전원 승낙 시 자동으로 라이브 트래킹 화면으로 전환하는 Side Effect를 발행한다.
 */
@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val meetPinRepository: MeetPinRepository
) : BaseViewModel<LobbyState, LobbyIntent, LobbyEffect>(LobbyState()) {

    override fun processIntent(intent: LobbyIntent) {
        when (intent) {
            is LobbyIntent.ObserveGroup -> observeGroup(intent.groupId)
        }
    }

    private fun observeGroup(groupId: String) {
        updateState { copy(groupId = groupId, isLoading = true) }

        meetPinRepository.observeGroup(groupId)
            .onEach { group ->
                updateState {
                    copy(
                        groupTitle = group.title,
                        participants = group.participants,
                        isAllAccepted = group.isAllAccepted,
                        groupStatus = group.status,
                        isLoading = false
                    )
                }

                // 전원 승낙 시 → ACTIVE 상태로 전환되면 라이브 트래킹으로 이동
                if (group.isAllAccepted || group.status == GroupStatus.ACTIVE) {
                    sendEffect(LobbyEffect.NavigateToLiveTracking(groupId))
                }
            }
            .catch { error ->
                updateState { copy(isLoading = false, errorMessage = error.message) }
                sendEffect(LobbyEffect.ShowError(error.message ?: "그룹 상태를 불러올 수 없습니다."))
            }
            .launchIn(viewModelScope)
    }
}
