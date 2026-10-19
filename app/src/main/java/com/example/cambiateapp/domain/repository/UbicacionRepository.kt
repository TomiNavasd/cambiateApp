package com.example.cambiateapp.domain.repository

import com.example.cambiateapp.domain.model.Coordenadas

interface UbicacionRepository {
    suspend fun ubicacionActual(): Coordenadas?
    suspend fun ultimaUbicacionConocida(): Coordenadas?
}
