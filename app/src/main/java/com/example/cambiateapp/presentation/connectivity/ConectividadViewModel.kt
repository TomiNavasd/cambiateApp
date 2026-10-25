package com.example.cambiateapp.presentation.connectivity


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.usecase.ObservarConectividadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ConectividadViewModel @Inject constructor(
    observarConectividad: ObservarConectividadUseCase
) : ViewModel() {

    val hayConexion: StateFlow<Boolean> = observarConectividad()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
}