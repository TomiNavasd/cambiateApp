package com.example.cambiateapp.presentation.notifications

sealed interface NotificacionesUiEvent {
    data object ProbarAhora : NotificacionesUiEvent
    data object PermisoRechazado : NotificacionesUiEvent
    data object AvisoMostrado : NotificacionesUiEvent
    data class ActivarCambiado(val activo: Boolean) : NotificacionesUiEvent
    data class HoraElegida(val hora: Int, val minuto: Int) : NotificacionesUiEvent
}