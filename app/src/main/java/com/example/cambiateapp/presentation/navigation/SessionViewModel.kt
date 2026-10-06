package com.example.cambiateapp.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.usecase.ObserveAuthStateUseCase
import com.example.cambiateapp.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

sealed interface SessionUiState {
    data object Cargando : SessionUiState
    data object SinSesion : SessionUiState
    data object ConSesion : SessionUiState
}

sealed interface SessionEvent {
    data object CerrarSesion : SessionEvent
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    observeAuthState: ObserveAuthStateUseCase,
    private val signOut: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Cargando)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
            .map { usuario ->
                if (usuario != null) SessionUiState.ConSesion else SessionUiState.SinSesion
            }
            .catch { emit(SessionUiState.SinSesion) }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun onEvent(evento: SessionEvent) {
        when (evento) {
            SessionEvent.CerrarSesion -> signOut()
        }
    }
}