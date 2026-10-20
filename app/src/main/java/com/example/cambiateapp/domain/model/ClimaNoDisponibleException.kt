package com.example.cambiateapp.domain.model

/** El pronóstico no se pudo obtener (sin conexión, servicio caído, respuesta inválida...). */
class ClimaNoDisponibleException(
    mensaje: String,
    causa: Throwable? = null,
) : Exception(mensaje, causa)