package com.example.cambiateapp.presentation.wardrobe

import com.example.cambiateapp.domain.model.Prenda

sealed interface DetalleUiState {
    data object Cargando : DetalleUiState
    data object NoEncontrada : DetalleUiState
    data class Error(val mensaje: String) : DetalleUiState
    data class Contenido(val prenda: Prenda) : DetalleUiState
}