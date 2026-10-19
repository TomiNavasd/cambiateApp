package com.example.cambiateapp.domain.repository

import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.model.Pronostico

interface ClimaRepository {
    suspend fun pronosticoDeManana(coordenadas: Coordenadas): Pronostico
}