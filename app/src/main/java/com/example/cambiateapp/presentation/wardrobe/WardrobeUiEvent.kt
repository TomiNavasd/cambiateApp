package com.example.cambiateapp.presentation.wardrobe

import com.example.cambiateapp.domain.model.Categoria

sealed interface WardrobeUiEvent {
    data class FiltroSeleccionado(val categoria: Categoria?) : WardrobeUiEvent
    data class BusquedaCambiada(val texto: String) : WardrobeUiEvent
    data object Reintentar : WardrobeUiEvent
}