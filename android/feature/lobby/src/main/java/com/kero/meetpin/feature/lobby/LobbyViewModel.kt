package com.kero.meetpin.feature.lobby

import androidx.lifecycle.viewModelScope
import com.kero.meetpin.core.designsystem.mvi.BaseViewModel
import com.kero.meetpin.core.domain.repository.MeetPinRepository
import com.kero.meetpin.core.model.GroupStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    /**
     * 그룹 관찰 코루틴. 재관찰 시 이전 것을 반드시 취소한다.
     *
     * `LobbyScreen`은 다른 목적지가 위에 쌓이면 dispose되고, 되돌아올 때 재구성되면서
     * [LobbyIntent.ObserveGroup]을 다시 보낸다. 이때 이전 collector를 취소하지 않으면
     * collector가 누적되어 [LobbyEffect.NavigateToLiveTracking]이 중복 발행되고,
     * 라이브 트래킹 목적지가 백스택에 여러 번 쌓인다.
     */
    private var observeJob: Job? = null

    private fun observeGroup(groupId: String) {
        updateState { copy(groupId = groupId, isLoading = true) }

        observeJob?.cancel()
        observeJob = meetPinRepository.observeGroup(groupId)
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
