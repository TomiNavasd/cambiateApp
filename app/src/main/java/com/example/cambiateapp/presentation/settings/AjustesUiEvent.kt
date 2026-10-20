package com.example.cambiateapp.presentation.settings

import com.example.cambiateapp.domain.model.ModoTema

sealed interface AjustesUiEvent {
    data class TemaElegido(val modo: ModoTema) : AjustesUiEvent
    data object AvisoMostrado : AjustesUiEvent
}