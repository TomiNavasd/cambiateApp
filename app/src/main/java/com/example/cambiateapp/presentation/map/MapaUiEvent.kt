package com.example.cambiateapp.presentation.map

sealed interface MapaUiEvent {
    data object PermisoConcedido : MapaUiEvent
    data object PermisoRechazado : MapaUiEvent
    data object Reintentar : MapaUiEvent
}