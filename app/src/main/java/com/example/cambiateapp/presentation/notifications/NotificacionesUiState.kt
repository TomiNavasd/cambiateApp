package com.example.cambiateapp.presentation.notifications

data class NotificacionesUiState(
    val activo: Boolean = false,
    val hora: Int = 20,
    val minuto: Int = 0,
    val aviso: String? = null,
)