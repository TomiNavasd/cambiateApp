package com.example.cambiateapp.data.preferences

import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.model.Pronostico

data class ClimaGuardado(val coordenadas: Coordenadas, val pronostico: Pronostico)

interface ClimaCache {
    suspend fun guardar(coordenadas: Coordenadas, pronostico: Pronostico)

    suspend fun leer(): ClimaGuardado?
}