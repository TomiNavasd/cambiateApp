package com.example.cambiateapp.presentation.wardrobe

sealed interface DetalleUiEvent {
    data object Reintentar : DetalleUiEvent
}