package com.example.cambiateapp.presentation.notifications

sealed interface NotificacionesUiEvent {
    data object ProbarAhora : NotificacionesUiEvent
    data object PermisoRechazado : NotificacionesUiEvent
    data object AvisoMostrado : NotificacionesUiEvent
}