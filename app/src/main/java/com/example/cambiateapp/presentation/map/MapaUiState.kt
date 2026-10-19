package com.example.cambiateapp.presentation.map

import com.example.cambiateapp.domain.model.Coordenadas

sealed interface MapaUiState {
    data class SinPermiso(val rechazado: Boolean = false) : MapaUiState
    data object Cargando : MapaUiState
    data class Contenido(val ubicacion: Coordenadas) : MapaUiState
    data class Error(val mensaje: String) : MapaUiState
}