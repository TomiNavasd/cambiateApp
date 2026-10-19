package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.repository.UbicacionRepository
import javax.inject.Inject

class ObtenerUbicacionUseCase @Inject constructor(
    private val ubicacionRepository: UbicacionRepository,
) {
    suspend operator fun invoke(): Coordenadas? = ubicacionRepository.ubicacionActual()
}