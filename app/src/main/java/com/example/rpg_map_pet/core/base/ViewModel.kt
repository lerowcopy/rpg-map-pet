package com.example.rpg_map_pet.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Base class for all ViewModels in the application.
 * Provides common functionality for handling UI events and state.
 */
abstract class BaseViewModel<STATE : UiState, EVENT : UiEvent> : ViewModel() {

    protected abstract val initialState: STATE

    private val _state = MutableSharedFlow<STATE>(replay = 1)
    val state: SharedFlow<STATE> = _state.asSharedFlow()

    private val _event = MutableSharedFlow<EVENT>()
    val event: SharedFlow<EVENT> = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.emit(initialState)
        }
    }

    protected fun setState(stateReducer: STATE.() -> STATE) {
        viewModelScope.launch {
            val currentState = _state.replayCache.firstOrNull() ?: initialState
            _state.emit(currentState.stateReducer())
        }
    }

    protected fun sendEvent(event: EVENT) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    /**
     * Handle errors from domain layer operations
     */
    protected fun handleError(error: Throwable, onError: (Throwable) -> Unit = {}) {
        onError(error)
        // Could add global error handling here (analytics, logging, etc.)
    }
}

/**
 * Base interface for all UI states
 */
interface UiState

/**
 * Base interface for all UI events
 */
interface UiEvent
