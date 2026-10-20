package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.model.Pronostico
import com.example.cambiateapp.domain.repository.ClimaRepository
import javax.inject.Inject

/** Pronóstico de mañana para un punto del mapa. */
class ObtenerPronosticoDeMananaUseCase @Inject constructor(
    private val climaRepository: ClimaRepository
) {
    suspend operator fun invoke(coordenadas: Coordenadas): Pronostico =
        climaRepository.pronosticoDeManana(coordenadas)
}