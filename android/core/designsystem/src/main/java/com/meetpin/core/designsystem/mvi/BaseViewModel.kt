package com.meetpin.core.designsystem.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI 패턴을 구동하는 추상 BaseViewModel.
 */
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /**
     * 현재 상태(State) 반환
     */
    protected val currentState: S
        get() = _uiState.value

    /**
     * UI에서 전달되는 Intent 처리
     */
    abstract fun processIntent(intent: I)

    /**
     * 상태 변경 헬퍼 함수
     */
    protected fun updateState(reducer: S.() -> S) {
        _uiState.update(reducer)
    }

    /**
     * 일회성 Side Effect 발행
     */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
