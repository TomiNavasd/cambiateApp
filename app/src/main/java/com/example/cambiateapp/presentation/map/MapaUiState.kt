package com.example.cambiateapp.presentation.map

import com.example.cambiateapp.domain.model.ConsejoClima
import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.model.Pronostico

/** Clima que se muestra sobre el mapa: los números y el consejo corto. */
data class ClimaUi(val pronostico: Pronostico, val consejo: ConsejoClima)

sealed interface MapaUiState {
    data class SinPermiso(val rechazado: Boolean = false) : MapaUiState
    data object Cargando : MapaUiState
    data class Contenido(
        val ubicacion: Coordenadas,
        val clima: ClimaUi? = null,
        val cargandoClima: Boolean = false
    ) : MapaUiState
    data class Error(val mensaje: String) : MapaUiState
}