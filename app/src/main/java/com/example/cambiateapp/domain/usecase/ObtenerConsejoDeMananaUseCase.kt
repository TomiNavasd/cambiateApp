package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.ConsejoClima
import com.example.cambiateapp.domain.repository.ClimaRepository
import com.example.cambiateapp.domain.repository.PrendaRepository
import com.example.cambiateapp.domain.repository.UbicacionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Junta ubicación, pronóstico y armario para armar el consejo de mañana. */
class ObtenerConsejoDeMananaUseCase @Inject constructor(
    private val ubicacionRepository: UbicacionRepository,
    private val climaRepository: ClimaRepository,
    private val prendaRepository: PrendaRepository,
    private val armarConsejo: ArmarConsejoClimaUseCase,
) {
    /** Devuelve null si todavía no conocemos la ubicación del usuario. */
    suspend operator fun invoke(): ConsejoClima? {
        val coordenadas = ubicacionRepository.ultimaUbicacionConocida() ?: return null
        val pronostico = climaRepository.pronosticoDeManana(coordenadas)
        val prendas = prendaRepository.observarPrendas().first()
        return armarConsejo(pronostico, prendas)
    }
}