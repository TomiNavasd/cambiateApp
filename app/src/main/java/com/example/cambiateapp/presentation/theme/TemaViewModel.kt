package com.example.cambiateapp.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.model.ModoTema
import com.example.cambiateapp.domain.usecase.ObservarModoTemaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Le dice a toda la app qué modo de tema eligió el usuario. */
@HiltViewModel
class TemaViewModel @Inject constructor(
    observarModoTema: ObservarModoTemaUseCase,
) : ViewModel() {

    val modo: StateFlow<ModoTema> = observarModoTema()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModoTema.SISTEMA)
}