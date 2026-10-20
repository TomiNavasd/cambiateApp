package com.example.cambiateapp.data.mapper

import com.example.cambiateapp.data.remote.ClimaDto
import com.example.cambiateapp.domain.model.ClimaNoDisponibleException
import com.example.cambiateapp.domain.model.Pronostico

/** En las listas de la API, la posición 0 es hoy y la 1 es mañana. */
private const val INDICE_MANANA = 1

fun ClimaDto.aPronosticoDeManana(): Pronostico {
    val diario = daily ?: throw ClimaNoDisponibleException("La respuesta no trae datos diarios")

    val minima = diario.temperaturaMinima?.getOrNull(INDICE_MANANA)
    val maxima = diario.temperaturaMaxima?.getOrNull(INDICE_MANANA)
    if (minima == null || maxima == null) {
        throw ClimaNoDisponibleException("La respuesta no trae las temperaturas de mañana")
    }

    // La probabilidad de lluvia puede venir null; en ese caso asumimos 0%.
    val lluvia = (diario.probabilidadLluviaMaxima?.getOrNull(INDICE_MANANA) ?: 0).coerceIn(0, 100)

    return Pronostico(tempMinC = minima, tempMaxC = maxima, probLluviaPct = lluvia)
}